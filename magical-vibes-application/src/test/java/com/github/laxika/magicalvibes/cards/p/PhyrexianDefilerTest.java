package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianDefilerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap and sacrifice ability gives a target creature -3/-3")
    void abilityGivesTargetCreatureMinusThreeMinusThree() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PhyrexianDefiler());
        source.setSummoningSick(false);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Defiler");
        harness.assertInGraveyard(player1, "Phyrexian Defiler");
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3/-3 effect wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PhyrexianDefiler());
        source.setSummoningSick(false);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreaturePermanent() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PhyrexianDefiler());
        source.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
