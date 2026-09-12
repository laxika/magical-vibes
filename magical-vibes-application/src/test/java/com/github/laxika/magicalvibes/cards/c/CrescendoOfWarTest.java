package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrescendoOfWar.class, GrizzlyBears.class})
class CrescendoOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a strife counter on itself at the beginning of each upkeep")
    void addsStrifeCounterAtEachUpkeep() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());

        resolveUpkeepTrigger(player1);
        resolveUpkeepTrigger(player2);

        assertThat(crescendo.getCounterCount(CounterType.STRIFE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creatures get +1/+0 for each strife counter")
    void boostsAttackingCreatures() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        crescendo.setCounterCount(CounterType.STRIFE, 2);
        Permanent ownAttacker = addReadyCreature(player1);
        Permanent ownIdle = addReadyCreature(player1);
        Permanent opponentAttacker = addReadyCreature(player2);
        ownAttacker.setAttacking(true);
        opponentAttacker.setAttacking(true);

        assertThat(gqs.computeStaticBonus(gd, ownAttacker).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, opponentAttacker).power()).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, ownIdle).power()).isZero();
    }

    @Test
    @DisplayName("Blocking creatures you control get +1/+0 for each strife counter")
    void boostsOwnBlockingCreatures() {
        Permanent crescendo = harness.addToBattlefieldAndReturn(player1, new CrescendoOfWar());
        crescendo.setCounterCount(CounterType.STRIFE, 3);
        Permanent ownBlocker = addReadyCreature(player1);
        Permanent ownIdle = addReadyCreature(player1);
        Permanent opponentBlocker = addReadyCreature(player2);
        ownBlocker.setBlocking(true);
        opponentBlocker.setBlocking(true);

        assertThat(gqs.computeStaticBonus(gd, ownBlocker).power()).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, opponentBlocker).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, ownIdle).power()).isZero();
    }

    private void resolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
