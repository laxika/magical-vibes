package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TifaLockhart.class, Forest.class})
class TifaLockhartTest extends BaseCardTest {

    @Test
    void doublesPowerWhenLandEnters() {
        Permanent tifa = addTifa();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tifa)).isEqualTo(2);
    }

    @Test
    void doublesCurrentPowerOnEachLandfallTrigger() {
        Permanent tifa = addTifa();

        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(2);

        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tifa)).isEqualTo(4);
    }

    private Permanent addTifa() {
        harness.addToBattlefield(player1, new TifaLockhart());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
