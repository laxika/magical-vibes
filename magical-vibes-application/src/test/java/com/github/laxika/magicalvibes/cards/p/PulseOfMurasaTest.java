package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PulseOfMurasaTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from any graveyard to its owner's hand and gains 6 life")
    void returnsCreatureFromOpponentsGraveyardAndGainsLife() {
        Card creature = new GrizzlyBears();
        harness.setLife(player1, 14);
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
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

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(land.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot target a card that is neither a creature nor a land")
    void cannotTargetNonCreatureNonland() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new PulseOfMurasa()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
