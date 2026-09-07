package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmptyTheCatacombs.class, GrizzlyBears.class, HolyDay.class})
class EmptyTheCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all creature cards from each player's graveyard to their hand")
    void returnsAllCreatureCardsFromEachPlayersGraveyardToTheirHand() {
        Card player1Creature = new GrizzlyBears();
        Card player1NonCreature = new HolyDay();
        Card player2Creature = new GrizzlyBears();
        Card player2NonCreature = new HolyDay();

        harness.setGraveyard(player1, List.of(player1Creature, player1NonCreature));
        harness.setGraveyard(player2, List.of(player2Creature, player2NonCreature));
        harness.setHand(player1, List.of(new EmptyTheCatacombs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(player1Creature.getId())
                .doesNotContain(player2Creature.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(player2Creature.getId())
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
