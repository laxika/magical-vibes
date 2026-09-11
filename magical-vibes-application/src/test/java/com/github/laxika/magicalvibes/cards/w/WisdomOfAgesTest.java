package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WisdomOfAgesTest extends BaseCardTest {

    private void addWisdomOfAgesMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);
    }

    @Test
    @DisplayName("Returns all instants and sorceries from the graveyard, but not permanents")
    void returnsAllInstantsAndSorceries() {
        Card instant = new Brainstorm();
        Card sorcery = new Divination();
        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(instant, sorcery, creature));

        WisdomOfAges wisdom = new WisdomOfAges();
        harness.setHand(player1, new ArrayList<>(List.of(wisdom)));
        addWisdomOfAgesMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(instant, sorcery);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Grants no maximum hand size for the rest of the game and exiles itself")
    void grantsNoMaximumHandSizeAndExilesItself() {
        WisdomOfAges wisdom = new WisdomOfAges();
        harness.setHand(player1, new ArrayList<>(List.of(wisdom)));
        addWisdomOfAgesMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wisdom);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wisdom);
    }
}
