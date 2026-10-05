package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KuldothaRingleader.class, GrizzlyBears.class})
class KuldothaRingleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Kuldotha Ringleader must attack when able")
    void mustAttackWhenAble() {
        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Kuldotha Ringleader can be declared as attacker and deals 4 damage")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities(); // resolve battle cry trigger (no other attackers to boost)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Kuldotha Ringleader does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new KuldothaRingleader());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        // Only bears can attack (index 1), ringleader has summoning sickness
        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attacking with Kuldotha Ringleader pushes battle cry trigger onto stack")
    void attackTriggerPushesOntoStack() {
        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(ringleader.getId());
    }

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities(); // resolve battle cry trigger

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(3); // 2 base + 1 battle cry
    }

    @Test
    @DisplayName("Battle cry does not boost Kuldotha Ringleader itself")
    void battleCryDoesNotBoostSelf() {
        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(ringleader.getPowerModifier()).isEqualTo(0);
        assertThat(ringleader.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void tappedRingleaderDoesNotHaveToAttack() {
        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        ringleader.setTapped(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(ringleader.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void battleCryDoesNotBoostNonattackers() {
        Permanent ringleader = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        ringleader.setSummoningSick(false);
        Permanent nonattacker = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KuldothaRingleader());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(nonattacker.getToughnessModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    void multipleRingleadersBoostEachOtherAndTheirBonusesStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        first.setSummoningSick(false);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        second.setSummoningSick(false);
        Permanent third = harness.addToBattlefieldAndReturn(player1, new KuldothaRingleader());
        third.setSummoningSick(false);

        declareAttackers(List.of(0, 1, 2));
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(third.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(third.getToughnessModifier()).isZero();
    }
}
