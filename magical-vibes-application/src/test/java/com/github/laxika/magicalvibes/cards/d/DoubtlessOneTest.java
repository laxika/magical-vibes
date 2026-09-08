package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CourtCleric;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoubtlessOne.class, CourtCleric.class})
class DoubtlessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Clerics on the battlefield")
    void powerAndToughnessCountBattlefieldClerics() {
        Permanent doubtlessOne = addCreatureReady(player1, new DoubtlessOne());

        assertThat(gqs.getEffectivePower(gd, doubtlessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, doubtlessOne)).isEqualTo(1);

        addCreatureReady(player1, new CourtCleric());
        addCreatureReady(player2, new CourtCleric());

        assertThat(gqs.getEffectivePower(gd, doubtlessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, doubtlessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains life equal to damage dealt")
    void gainsLifeEqualToDamageDealt() {
        addAttacker(new DoubtlessOne());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent addAttacker(DoubtlessOne card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
