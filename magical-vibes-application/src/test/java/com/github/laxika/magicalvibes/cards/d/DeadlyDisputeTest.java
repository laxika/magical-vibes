package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.p.PaladinClass;
import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyDispute.class, HillGiantHerdgorger.class, SpareDagger.class, PaladinClass.class})
class DeadlyDisputeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new DeadlyDispute()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertNotOnBattlefield(player1, "Hill Giant Herdgorger");
        harness.assertInGraveyard(player1, "Hill Giant Herdgorger");
    }

    @Test
    @DisplayName("Sacrifices an artifact, draws two cards, and creates a Treasure")
    void sacrificesArtifactDrawsAndCreatesTreasure() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SpareDagger());
        harness.setHand(player1, List.of(new DeadlyDispute()));
        addMana();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Spare Dagger");
        harness.assertInGraveyard(player1, "Deadly Dispute");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-creature permanent")
    void cannotSacrificeNonArtifactNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        harness.setHand(player1, List.of(new DeadlyDispute()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    @DisplayName("Cannot cast without an artifact or creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new DeadlyDispute()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's artifact")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new SpareDagger());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new SpareDagger());
        harness.setHand(player1, List.of(new DeadlyDispute()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opposingArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");

        harness.assertOnBattlefield(player1, "Spare Dagger");
        harness.assertOnBattlefield(player2, "Spare Dagger");
        harness.assertInHand(player1, "Deadly Dispute");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draw and Treasure creation wait for resolution after sacrificing a tapped creature")
    void drawsAndCreatesTreasureOnlyOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new DeadlyDispute()));
        harness.setLibrary(player1, List.of(new SpareDagger(), new PaladinClass()));
        addMana();

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());

        harness.assertInGraveyard(player1, "Hill Giant Herdgorger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Spare Dagger");
        harness.assertInHand(player1, "Paladin Class");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure").getFirst().isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Deadly Dispute");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
