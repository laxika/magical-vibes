package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReapThePast.class, GrizzlyBears.class})
class ReapThePastTest extends BaseCardTest {

    @Test
    void returnsXRandomCardsAndExilesTheSpell() {
        List<Card> graveyardCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        ReapThePast reapThePast = new ReapThePast();
        harness.setGraveyard(player1, graveyardCards);
        harness.setHand(player1, List.of(reapThePast));
        addManaForXTwo();

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(graveyardCards).contains(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(gd.findExiledCard(reapThePast.getId())).isNotNull();
    }

    @Test
    void returnsNoCardsForZeroXAndStillExilesTheSpell() {
        ReapThePast reapThePast = new ReapThePast();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(reapThePast));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(reapThePast.getId())).isNotNull();
    }

    private void addManaForXTwo() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
