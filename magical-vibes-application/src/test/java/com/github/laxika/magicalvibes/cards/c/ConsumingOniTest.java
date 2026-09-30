package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingOni.class, Forest.class, GrizzlyBears.class})
class ConsumingOniTest extends BaseCardTest {

    @Test
    void endStepMarksOneRandomNonlandCardInHand() {
        addCreatureReady(player1, new ConsumingOni());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            gs.advanceStep(gd);
            resolveAllTriggers();
        });

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }
}
