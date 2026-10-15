package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MoorishCavalry;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VhatiIlDal.class, MoorishCavalry.class, TormodsCrypt.class})
class VhatiIlDalTest extends BaseCardTest {

    private static final String POWER_MODE = "It has base power 1";
    private static final String TOUGHNESS_MODE = "It has base toughness 1";

    private Permanent setUpVhati() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new VhatiIlDal());
        return harness.addToBattlefieldAndReturn(player1, new MoorishCavalry());
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
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Toughness mode sets base toughness to 1 and leaves base power alone")
    void toughnessMode() {
        Permanent drake = setUpVhati();

        activate(drake, TOUGHNESS_MODE);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
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

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new VhatiIlDal());
        Permanent drake = addCreatureReady(player2, new MoorishCavalry());

        activate(drake, POWER_MODE);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can target only a creature")
    void cannotTargetNoncreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new VhatiIlDal());
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new TormodsCrypt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, crypt.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Activating the ability taps Vhati il-Dal")
    void activationTapsVhati() {
        Permanent drake = setUpVhati();

        activate(drake, POWER_MODE);

        assertThat(findPermanent(player1, "Vhati il-Dal").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated while Vhati il-Dal is tapped")
    void cannotActivateWhenTapped() {
        Permanent drake = setUpVhati();
        Permanent vhati = findPermanent(player1, "Vhati il-Dal");
        vhati.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, drake.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Power setting preserves counters and the other base value")
    void powerModePreservesCounters() {
        Permanent target = setUpVhati();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activate(target, POWER_MODE);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Toughness setting preserves counters and the other base value")
    void toughnessModePreservesCounters() {
        Permanent target = setUpVhati();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activate(target, TOUGHNESS_MODE);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Separate activations can set both base values to 1")
    void separateActivationsSetBothBaseValues() {
        Permanent target = setUpVhati();
        activate(target, POWER_MODE);
        findPermanent(player1, "Vhati il-Dal").untap();
        harness.clearPriorityPassed();

        activate(target, TOUGHNESS_MODE);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vhati can target itself")
    void canTargetItself() {
        setUpVhati();
        Permanent vhati = findPermanent(player1, "Vhati il-Dal");

        activate(vhati, TOUGHNESS_MODE);

        assertThat(gqs.getEffectivePower(gd, vhati)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vhati)).isEqualTo(1);
        assertThat(vhati.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent target = setUpVhati();
        findPermanent(player1, "Vhati il-Dal").setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Choosing toughness kills a creature with lethal marked damage")
    void toughnessModeMakesMarkedDamageLethal() {
        Permanent target = setUpVhati();
        target.setMarkedDamage(1);

        activate(target, TOUGHNESS_MODE);

        harness.assertNotOnBattlefield(player1, "Moorish Cavalry");
        harness.assertInGraveyard(player1, "Moorish Cavalry");
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents the choice")
    void missingTargetDoesNotPromptForChoice() {
        Permanent target = setUpVhati();
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Moorish Cavalry");
    }

    @Test
    @DisplayName("The toughness setting also expires at end of turn")
    void toughnessModeWearsOffAtEndOfTurn() {
        Permanent target = setUpVhati();
        activate(target, TOUGHNESS_MODE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
