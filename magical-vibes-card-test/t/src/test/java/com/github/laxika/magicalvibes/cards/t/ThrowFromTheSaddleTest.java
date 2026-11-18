package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrowFromTheSaddle.class, GrizzlyBears.class, HillGiant.class, TrainedArynx.class})
class ThrowFromTheSaddleTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Mount gets +1/+1 until end of turn and deals its boosted power")
    void boostsNonMountAndDealsBoostedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castThrow(target);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
    }

    @Test
    @DisplayName("A Mount gets a +1/+1 counter instead and deals its power")
    void putsCounterOnMountAndDealsPower() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        harness.addToBattlefield(player2, new HillGiant());
        castThrow(mount);

        assertThat(PermanentGrants.powerBoostOf(gd, mount)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, mount)).isZero();
        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mountDealsPowerAfterCounterWithoutTakingReturnDamage() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        PermanentGrants.setToughnessBoost(gd, victim, 2);

        castThrow(mount);

        assertThat(victim.getMarkedDamage()).isEqualTo(4);
        assertThat(mount.getMarkedDamage()).isZero();
        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Trained Arynx");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void stillBoostsCreatureWhenDamageTargetBecomesIllegal() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        queueThrow(source);
        PermanentGrants.keywordUntilEndOfTurn(gd, victim, Keyword.HEXPROOF);

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, source)).isEqualTo(1);
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void stillPutsCounterOnMountWhenDamageTargetBecomesIllegal() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        queueThrow(mount);
        PermanentGrants.keywordUntilEndOfTurn(gd, victim, Keyword.HEXPROOF);

        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, mount)).isZero();
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageWhenSourceBecomesIllegal() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        queueThrow(source);
        PermanentGrants.keywordUntilEndOfTurn(gd, source, Keyword.SHROUD);

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, source)).isZero();
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The spell requires a creature you control and a creature you do not control")
    void restrictsTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ThrowFromTheSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castThrow(Permanent target) {
        harness.setHand(player1, List.of(new ThrowFromTheSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(),
                harness.getPermanentId(player2, "Hill Giant")));
    }

    private void queueThrow(Permanent target) {
        harness.setHand(player1, List.of(new ThrowFromTheSaddle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(target.getId(),
                harness.getPermanentId(player2, "Hill Giant")));
    }
}
