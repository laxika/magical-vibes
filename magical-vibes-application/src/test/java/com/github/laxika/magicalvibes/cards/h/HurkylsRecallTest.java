package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HurkylsRecall.class, AngelsFeather.class, GrizzlyBears.class, IcyManipulator.class})
class HurkylsRecallTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Returns all artifacts target player owns to their hand")
    void returnsAllArtifactsToTargetPlayersHand() {
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // No artifacts on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.ARTIFACT));

        // Both artifacts in player2's hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Angel's Feather", "Icy Manipulator");
    }

    @Test
    @DisplayName("Can target self to return own artifacts")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.ARTIFACT));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Angel's Feather");
    }

    @Test
    @DisplayName("Does not return non-artifact permanents")
    void doesNotReturnNonArtifactPermanents() {
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Creature should still be on battlefield
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        // Artifact should be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Angel's Feather");
    }

    @Test
    @DisplayName("Returns an artifact owned by the target player even when another player controls it")
    void returnsArtifactOwnedByTargetPlayerUnderOpponentsControl() {
        Permanent stolenArtifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        gd.stolenCreatures.put(stolenArtifact.getId(), player2.getId());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player1, "Icy Manipulator");
        harness.assertInHand(player2, "Icy Manipulator");
    }

    @Test
    @DisplayName("Does not affect other player's artifacts")
    void doesNotAffectOtherPlayersArtifacts() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Player1's artifact should still be on battlefield
        harness.assertOnBattlefield(player1, "Angel's Feather");

        // Player2's artifact should be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Icy Manipulator");
    }

    @Test
    @DisplayName("Works when target player has no artifacts")
    void worksWithNoArtifacts() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Creature still on battlefield
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hurkyl's Recall goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hurkyl's Recall");
    }
}

