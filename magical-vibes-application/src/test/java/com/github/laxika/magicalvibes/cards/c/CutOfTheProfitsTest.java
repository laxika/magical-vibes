package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutOfTheProfits.class, GrizzlyBears.class, HillGiant.class})
class CutOfTheProfitsTest extends BaseCardTest {

    @Test
    void drawsAndLosesLifeEqualToX() {
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.setLibrary(player1, cards(6));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void casualtyCopiesTheSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new CutOfTheProfits()));
        harness.setLibrary(player1, cards(6));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
