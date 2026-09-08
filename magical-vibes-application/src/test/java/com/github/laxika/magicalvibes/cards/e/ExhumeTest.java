package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExhumeTest extends BaseCardTest {

    @Test
    @DisplayName("Each player returns one creature card from their graveyard")
    void eachPlayerReturnsOneCreatureCard() {
        Card player1Creature = new GrizzlyBears();
        Card player2Creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(player1Creature));
        harness.setGraveyard(player2, List.of(player2Creature));

        castExhume();

        assertThat(battlefieldCards(player1)).containsExactly(player1Creature);
        assertThat(battlefieldCards(player2)).containsExactly(player2Creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(player2Creature);
    }

    @Test
    @DisplayName("Leaves noncreature cards and additional creature cards in graveyards")
    void returnsOnlyOneCreaturePerPlayer() {
        Card returned = new GrizzlyBears();
        Card remainingCreature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(returned, remainingCreature, instant));

        castExhume();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(battlefieldCards(player1)).containsExactly(returned);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(remainingCreature, instant)
                .doesNotContain(returned);
    }

    private void castExhume() {
        harness.setHand(player1, List.of(new Exhume()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Card> battlefieldCards(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(permanent -> permanent.getCard())
                .toList();
    }
}
