package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.g.GrowthCycle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PulseOfMurasa.class, GreenwoodSentinel.class, GrowthCycle.class, EvolvingWilds.class})
class PulseOfMurasaTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from any graveyard to its owner's hand and gains 6 life")
    void returnsCreatureFromOpponentsGraveyardAndGainsLife() {
        Card creature = new GreenwoodSentinel();
        harness.setLife(player1, 14);
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Returns a target land from a graveyard to its owner's hand and gains 6 life")
    void returnsLandAndGainsLife() {
        Card land = new EvolvingWilds();
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Evolving Wilds");
        harness.assertNotInGraveyard(player1, "Evolving Wilds");
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Cannot target a card that is neither a creature nor a land")
    void cannotTargetNonCreatureNonland() {
        Card instant = new GrowthCycle();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns an opponent's land to its owner while only the caster gains life")
    void returnsOpponentsLandAndOnlyCasterGainsLife() {
        Card land = new EvolvingWilds();
        harness.setLife(player1, 12);
        harness.setLife(player2, 9);
        harness.setGraveyard(player2, List.of(land));
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Evolving Wilds");
        harness.assertNotInHand(player1, "Evolving Wilds");
        harness.assertNotInGraveyard(player2, "Evolving Wilds");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Does not gain life when another Pulse returns the only target first")
    void doesNotGainLifeWhenTargetLeavesGraveyardBeforeResolution() {
        Card creature = new GreenwoodSentinel();
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PulseOfMurasa(), new PulseOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player1, "Greenwood Sentinel");
        harness.assertLife(player1, 16);

        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof PulseOfMurasa).hasSize(2);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target just to gain life")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Pulse of Murasa");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
