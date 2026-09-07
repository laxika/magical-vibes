package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.k.KjeldoranEscort;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnlikelyAlliance.class, KjeldoranEscort.class})
class UnlikelyAllianceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +0/+2 to a nonattacking, nonblocking creature")
    void boostsIdleCreature() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent escort = addCreatureReady(player1, new KjeldoranEscort());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, escort.getId());
        harness.passBothPriorities();

        assertThat(escort.getEffectivePower()).isEqualTo(2);
        assertThat(escort.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent escort = addCreatureReady(player1, new KjeldoranEscort());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, escort.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(escort.getToughnessModifier()).isEqualTo(0);
        assertThat(escort.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a nonattacking, nonblocking creature an opponent controls")
    void boostsOpponentCreature() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent escort = addCreatureReady(player2, new KjeldoranEscort());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, escort.getId());
        harness.passBothPriorities();

        assertThat(escort.getEffectivePower()).isEqualTo(2);
        assertThat(escort.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent otherAlliance = harness.addToBattlefieldAndReturn(player1, new UnlikelyAlliance());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherAlliance.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Becomes illegal if the target attacks before resolution")
    void targetMustStillBeNonattackingOnResolution() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent escort = addCreatureReady(player1, new KjeldoranEscort());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, escort.getId());
        escort.setAttacking(true);
        harness.passBothPriorities();

        assertThat(escort.getEffectivePower()).isEqualTo(2);
        assertThat(escort.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an attacking creature")
    void cannotTargetAttackingCreature() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent attacker = addCreatureReady(player1, new KjeldoranEscort());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not be attacking or blocking");
    }

    @Test
    @DisplayName("Cannot target a blocking creature")
    void cannotTargetBlockingCreature() {
        harness.addToBattlefield(player1, new UnlikelyAlliance());
        Permanent blocker = addCreatureReady(player1, new KjeldoranEscort());
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not be attacking or blocking");
    }
}
