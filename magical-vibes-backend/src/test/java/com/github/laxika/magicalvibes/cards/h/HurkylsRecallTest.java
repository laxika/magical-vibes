package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HurkylsRecallTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Hurkyl's Recall has correct card properties")
    void hasCorrectProperties() {
        HurkylsRecall card = new HurkylsRecall();

        assertThat(card.getName()).isEqualTo("Hurkyl's Recall");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnArtifactsTargetPlayerOwnsToHandEffect.class);
    }

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
        assertThat(entry.getCard().getName()).isEqualTo("Hurkyl's Recall");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Returns all artifacts target player owns to their hand")
    void returnsAllArtifactsToTargetPlayersHand() {
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No artifacts on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getType() == CardType.ARTIFACT);

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

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getType() == CardType.ARTIFACT);

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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Creature should still be on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Artifact should be in hand
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Angel's Feather");
    }

    @Test
    @DisplayName("Does not affect other player's artifacts")
    void doesNotAffectOtherPlayersArtifacts() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1's artifact should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel's Feather"));

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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Creature still on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hurkyl's Recall goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.setHand(player1, List.of(new HurkylsRecall()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hurkyl's Recall"));
    }
}

