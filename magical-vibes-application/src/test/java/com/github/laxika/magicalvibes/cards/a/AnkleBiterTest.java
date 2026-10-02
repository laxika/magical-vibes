package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SpinewoodsArmadillo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnkleBiter.class, SpinewoodsArmadillo.class})
class AnkleBiterTest extends BaseCardTest {

    @Test
    @DisplayName("Ankle Biter destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent ankleBiter = harness.addToBattlefieldAndReturn(player1, new AnkleBiter());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SpinewoodsArmadillo());

        ankleBiter.setSummoningSick(false);
        ankleBiter.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ankleBiter.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Ankle Biter destroys a larger attacker while blocking")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SpinewoodsArmadillo());
        Permanent ankleBiter = harness.addToBattlefieldAndReturn(player2, new AnkleBiter());

        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        ankleBiter.setBlocking(true);
        ankleBiter.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ankleBiter.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
