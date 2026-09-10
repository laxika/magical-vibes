package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonewoodInvoker.class})
class StonewoodInvokerTest extends BaseCardTest {

    @Test
    void resolvingAbilityBoostsSelf() {
        Permanent invoker = addReadyInvoker(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(5);
        assertThat(invoker.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    void repeatedActivationsStack() {
        Permanent invoker = addReadyInvoker(player1);
        addActivationMana(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(10);
        assertThat(invoker.getToughnessModifier()).isEqualTo(10);
    }

    @Test
    void abilityDoesNotRequireTapping() {
        Permanent invoker = addReadyInvoker(player1);
        invoker.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent invoker = addReadyInvoker(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(0);
        assertThat(invoker.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReadyInvoker(Player player) {
        Permanent permanent = new Permanent(new StonewoodInvoker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana() {
        addActivationMana(1);
    }

    private void addActivationMana(int activations) {
        harness.addMana(player1, ManaColor.GREEN, activations);
        harness.addMana(player1, ManaColor.COLORLESS, activations * 7);
    }
}
