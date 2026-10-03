package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.j.Jokulmorder;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscipleOfTeveshSzat.class, BorealCentaur.class, SnowCoveredForest.class, Jokulmorder.class})
class DiscipleOfTeveshSzatTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability gives target creature -1/-1 until end of turn")
    void givesTargetCreatureMinusOneMinusOne() {
        addReadyDisciple();
        Permanent target = addCreatureReady(player2, new BorealCentaur());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-1/-1 effect wears off at end of turn")
    void minusOneMinusOneWearsOff() {
        addReadyDisciple();
        Permanent target = addCreatureReady(player2, new BorealCentaur());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tap ability cannot be activated while Disciple of Tevesh Szat is tapped")
    void tapAbilityRequiresUntappedSource() {
        addReadyDisciple();
        Permanent target = addCreatureReady(player2, new BorealCentaur());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paid ability sacrifices itself and gives target creature -6/-6")
    void sacrificesItselfForMinusSixMinusSix() {
        Permanent disciple = addReadyDisciple();
        Permanent target = addCreatureReady(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Tevesh Szat");
        harness.assertNotOnBattlefield(player2, "Boreal Centaur");
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Neither ability can target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyDisciple();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = land.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeResolutionAndMinusSixWearsOff() {
        addReadyDisciple();
        Permanent target = addCreatureReady(player2, new Jokulmorder());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Disciple of Tevesh Szat");
        harness.assertNotOnBattlefield(player1, "Disciple of Tevesh Szat");
        assertThat(target.getEffectivePower()).isEqualTo(12);
        assertThat(target.getEffectiveToughness()).isEqualTo(12);

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(12);
        assertThat(target.getEffectiveToughness()).isEqualTo(12);
    }

    @Test
    void sacrificeAbilityRequiresTwoBlackMana() {
        Permanent disciple = addReadyDisciple();
        Permanent target = addCreatureReady(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(disciple.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Disciple of Tevesh Szat");
        harness.assertNotInGraveyard(player1, "Disciple of Tevesh Szat");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothAbilitiesRequireSourceWithoutSummoningSickness() {
        Permanent disciple = addReadyDisciple();
        disciple.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(disciple.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Disciple of Tevesh Szat");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tapAbilityCanTargetItselfAndKillItWithZeroToughness() {
        Permanent disciple = addReadyDisciple();

        harness.activateAbility(player1, 0, null, disciple.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Disciple of Tevesh Szat");
        harness.assertInGraveyard(player1, "Disciple of Tevesh Szat");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeAbilityCanTargetItselfButHasNoLegalTargetOnResolution() {
        Permanent disciple = addReadyDisciple();
        Permanent otherCreature = addCreatureReady(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, disciple.getId());

        harness.assertInGraveyard(player1, "Disciple of Tevesh Szat");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyDisciple() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfTeveshSzat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return disciple;
    }
}
