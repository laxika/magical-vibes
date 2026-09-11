package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VhatiIlDal.class, FightingDrake.class, Forest.class})
class VhatiIlDalTest extends BaseCardTest {

    private static final String POWER_MODE = "It has base power 1";
    private static final String TOUGHNESS_MODE = "It has base toughness 1";

    private Permanent setUpVhati() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new VhatiIlDal());
        return harness.addToBattlefieldAndReturn(player1, new FightingDrake());
    }

    private void activate(Permanent target, String mode) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    @Test
    @DisplayName("Power mode sets base power to 1 and leaves base toughness alone")
    void powerMode() {
        Permanent drake = setUpVhati();

        activate(drake, POWER_MODE);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Toughness mode sets base toughness to 1 and leaves base power alone")
    void toughnessMode() {
        Permanent drake = setUpVhati();

        activate(drake, TOUGHNESS_MODE);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(1);
    }

    @Test
    @DisplayName("The base power set wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent drake = setUpVhati();

        activate(drake, POWER_MODE);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new VhatiIlDal());
        Permanent drake = addCreatureReady(player2, new FightingDrake());

        activate(drake, POWER_MODE);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new VhatiIlDal());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
