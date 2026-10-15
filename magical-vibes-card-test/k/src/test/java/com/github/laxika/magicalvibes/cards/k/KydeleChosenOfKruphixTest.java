package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KydeleChosenOfKruphix.class)
class KydeleChosenOfKruphixTest extends BaseCardTest {

    @Test
    void tapsForColorlessManaEqualToCardsDrawnThisTurn() {
        Permanent kydele = addCreatureReady(player1, new KydeleChosenOfKruphix());
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(kydele.isTapped()).isTrue();
    }

    @Test
    void producesNoManaWhenNoCardsWereDrawnThisTurn() {
        addCreatureReady(player1, new KydeleChosenOfKruphix());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
