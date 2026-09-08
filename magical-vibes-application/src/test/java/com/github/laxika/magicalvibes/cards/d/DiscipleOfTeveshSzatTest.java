package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscipleOfTeveshSzatTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability gives target creature -1/-1 until end of turn")
    void givesTargetCreatureMinusOneMinusOne() {
        addReadyDisciple();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-1/-1 effect wears off at end of turn")
    void minusOneMinusOneWearsOff() {
        addReadyDisciple();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paid ability sacrifices itself and gives target creature -6/-6")
    void sacrificesItselfForMinusSixMinusSix() {
        Permanent disciple = addReadyDisciple();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Tevesh Szat");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Neither ability can target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyDisciple();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        UUID targetId = land.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDisciple() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTeveshSzat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return disciple;
    }
}
