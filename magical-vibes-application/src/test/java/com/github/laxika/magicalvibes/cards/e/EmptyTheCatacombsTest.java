package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmptyTheCatacombs.class, BorosRecruit.class, BorosSignet.class})
class EmptyTheCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all creature cards from each player's graveyard to their hand")
    void returnsAllCreatureCardsFromEachPlayersGraveyardToTheirHand() {
        Card player1Creature = new BorosRecruit();
        Card player1SecondCreature = new BorosRecruit();
        Card player1NonCreature = new BorosSignet();
        Card player2Creature = new BorosRecruit();
        Card player2SecondCreature = new BorosRecruit();
        Card player2NonCreature = new BorosSignet();

        harness.setGraveyard(player1, List.of(player1Creature, player1SecondCreature, player1NonCreature));
        harness.setGraveyard(player2, List.of(player2Creature, player2SecondCreature, player2NonCreature));
        harness.castFromHand(player1, new EmptyTheCatacombs(), "{3}{B}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(player1Creature.getId())
                .contains(player1SecondCreature.getId())
                .doesNotContain(player2Creature.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(player2Creature.getId())
                .contains(player2SecondCreature.getId())
                .doesNotContain(player1Creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(player1NonCreature.getId())
                .doesNotContain(player1Creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(player2NonCreature.getId())
                .doesNotContain(player2Creature.getId());
    }
}
