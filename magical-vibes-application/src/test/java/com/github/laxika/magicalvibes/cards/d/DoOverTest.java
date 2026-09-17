package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoOver.class, GrizzlyBears.class})
class DoOverTest extends BaseCardTest {

    @Test
    void restartsTheTurnAndExilesDoOver() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoOver()));

        gd.currentStep = TurnStep.UNTAP;
        gd.captureTurnStartSnapshot();

        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.playerLifeTotals.put(player1.getId(), 10);
        bear.tap();
        DoOver doOver = (DoOver) gd.playerHands.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .extracting(Permanent::isTapped).isEqualTo(false);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(doOver);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(doOver);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == doOver);
    }
}
