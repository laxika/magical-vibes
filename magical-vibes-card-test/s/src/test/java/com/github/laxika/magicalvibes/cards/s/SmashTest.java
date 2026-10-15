package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.p.PrivilegedPosition;
import com.github.laxika.magicalvibes.model.GameData;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Smash.class, BorosSignet.class, GlassGolem.class, BorosRecruit.class,
        DarksteelIngot.class, PrivilegedPosition.class})
class SmashTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Smash puts it on the stack with target")
    void castingPutsOnStack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        Smash smash = new Smash();
        harness.setHand(player1, List.of(smash));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isSameAs(smash);
        assertThat(entry.getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Resolving Smash destroys target artifact and draws a card")
    void destroysArtifactAndDraws() {
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.castAndResolveInstant(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Boros Signet");
        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Resolving Smash destroys an artifact creature and draws a card")
    void destroysArtifactCreatureAndDraws() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, artifactCreature.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Glass Golem");
        harness.assertInGraveyard(player2, "Glass Golem");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Can destroy own artifact with Smash")
    void canDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Glass Golem");
        harness.assertInGraveyard(player1, "Glass Golem");
    }

    @Test
    @DisplayName("Smash goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Smash");
    }

    @Test
    @DisplayName("Smash fizzles and does not draw when target is removed before resolution")
    void fizzlesAndDoesNotDrawWhenTargetRemoved() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        harness.assertInGraveyard(player1, "Smash");
    }

    @Test
    @DisplayName("Cannot destroy a creature with Smash")
    void cannotDestroyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Smash draws a card even when the artifact is indestructible")
    void drawsWhenArtifactIsIndestructible() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        BorosRecruit drawnCard = new BorosRecruit();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        harness.assertNotInGraveyard(player2, "Darksteel Ingot");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Smash");
    }

    @Test
    @DisplayName("Smash does not destroy or draw when its target gains hexproof")
    void doesNotDrawWhenTargetGainsHexproof() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        BorosRecruit libraryCard = new BorosRecruit();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new Smash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, artifact.getId());
        harness.addToBattlefield(player2, new PrivilegedPosition());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boros Signet");
        harness.assertNotInGraveyard(player2, "Boros Signet");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Smash");
    }
}

