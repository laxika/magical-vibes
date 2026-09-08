package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MireTriton.class, Forest.class})
class MireTritonTest extends BaseCardTest {

    @Test
    void enteringBattlefieldMillsTwoCardsAndGainsTwoLife() {
        Forest firstMilledCard = new Forest();
        Forest secondMilledCard = new Forest();
        harness.setLibrary(player1, List.of(firstMilledCard, secondMilledCard));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MireTriton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilledCard, secondMilledCard);
        harness.assertLife(player1, 12);
    }
}
