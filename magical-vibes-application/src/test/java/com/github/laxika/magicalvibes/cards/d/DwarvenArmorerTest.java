package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DwarvenArmorer.class, DwarvenHold.class})
class DwarvenArmorerTest extends BaseCardTest {

    private static final String TOUGHNESS_MODE = "Put a +0/+1 counter on it";
    private static final String POWER_MODE = "Put a +1/+0 counter on it";

    @Test
    @DisplayName("The +0/+1 mode puts a +0/+1 counter on the target creature")
    void putsToughnessCounter() {
        Permanent target = setUpArmorerAndTarget();
        int power = gqs.getEffectivePower(gd, target);
        int toughness = gqs.getEffectiveToughness(gd, target);

        activate(target, TOUGHNESS_MODE);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness + 1);
        harness.assertInGraveyard(player1, "Dwarven Hold");
    }

    @Test
    @DisplayName("The +1/+0 mode puts a +1/+0 counter on the target creature")
    void putsPowerCounter() {
        Permanent target = setUpArmorerAndTarget();
        int power = gqs.getEffectivePower(gd, target);
        int toughness = gqs.getEffectiveToughness(gd, target);

        activate(target, POWER_MODE);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("The ability can target a creature controlled by another player")
    void canTargetOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new DwarvenArmorer());
        Permanent target = addCreatureReady(player2, new DwarvenArmorer());
        int toughness = gqs.getEffectiveToughness(gd, target);

        activate(target, TOUGHNESS_MODE);

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness + 1);
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new DwarvenArmorer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DwarvenHold());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new DwarvenHold()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent setUpArmorerAndTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new DwarvenArmorer());
        return harness.addToBattlefieldAndReturn(player1, new DwarvenArmorer());
    }

    private void activate(Permanent target, String mode) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new DwarvenHold()));
        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
