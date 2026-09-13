package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LaccolithGrunt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfStrength.class, LaccolithGrunt.class, SealOfRemoval.class})
class SealOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Seal of Strength sacrifices it as a cost")
    void sacrificesAsCost() {
        harness.addToBattlefield(player1, new SealOfStrength());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LaccolithGrunt());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Strength");
        harness.assertInGraveyard(player1, "Seal of Strength");
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice ability gives target creature +3/+3 and sacrifices Seal of Strength")
    void sacrificeAbilityBoostsTargetCreature() {
        harness.addToBattlefield(player1, new SealOfStrength());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LaccolithGrunt());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Seal of Strength");
        harness.assertInGraveyard(player1, "Seal of Strength");
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void boostsOwnCreature() {
        harness.addToBattlefield(player1, new SealOfStrength());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LaccolithGrunt());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("+3/+3 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SealOfStrength());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LaccolithGrunt());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability does nothing if its target leaves before resolution")
    void abilityFizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SealOfStrength());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LaccolithGrunt());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Seal of Strength");
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new SealOfStrength());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfRemoval());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Seal of Strength");
        assertThat(gd.stack).isEmpty();
    }
}
