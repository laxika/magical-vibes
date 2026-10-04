package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Savor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuskbursterSwarm.class, Savor.class})
class HuskbursterSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each owned creature card in the graveyard and exile")
    void costsLessForOwnedCreatureCardsInGraveyardAndExile() {
        harness.setGraveyard(player1, List.of(new HuskbursterSwarm(), new Savor()));
        harness.setExile(player1, List.of(new HuskbursterSwarm()));
        harness.setGraveyard(player2, List.of(new HuskbursterSwarm()));
        harness.setExile(player2, List.of(new HuskbursterSwarm()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Huskburster Swarm");
    }

    @Test
    @DisplayName("Does not count noncreature cards or creature cards owned by an opponent")
    void doesNotCountNoncreatureOrOpponentOwnedCards() {
        harness.setGraveyard(player1, List.of(new HuskbursterSwarm(), new Savor()));
        harness.setExile(player1, List.of(new Savor()));
        harness.setGraveyard(player2, List.of(new HuskbursterSwarm(), new HuskbursterSwarm()));
        harness.setExile(player2, List.of(new HuskbursterSwarm()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void graveyardAloneReducesCost() {
        harness.setGraveyard(player1, List.of(new HuskbursterSwarm()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Huskburster Swarm");
    }

    @Test
    void exileAloneReducesCost() {
        harness.setExile(player1, List.of(new HuskbursterSwarm()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Huskburster Swarm");
    }

    @Test
    void excessReductionStillAllowsCastingForOneBlackMana() {
        harness.setGraveyard(player1, List.of(
                new HuskbursterSwarm(), new HuskbursterSwarm(), new HuskbursterSwarm(),
                new HuskbursterSwarm(), new HuskbursterSwarm()));
        harness.setExile(player1, List.of(
                new HuskbursterSwarm(), new HuskbursterSwarm(), new HuskbursterSwarm()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Huskburster Swarm");
    }

    @Test
    void reductionDoesNotPayTheBlackManaRequirement() {
        harness.setExile(player1, List.of(
                new HuskbursterSwarm(), new HuskbursterSwarm(), new HuskbursterSwarm(),
                new HuskbursterSwarm(), new HuskbursterSwarm(), new HuskbursterSwarm(),
                new HuskbursterSwarm(), new HuskbursterSwarm()));
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void faceDownExiledCreatureDoesNotReduceCost() {
        gd.addToExile(player1.getId(), new HuskbursterSwarm(), null, true);
        harness.setHand(player1, List.of(new HuskbursterSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
