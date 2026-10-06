package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OranRiefInvoker.class})
class OranRiefInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Eight mana gives Oran-Rief Invoker +5/+5 and trample until end of turn")
    void abilityBoostsSelfAndGrantsTrample() {
        Permanent invoker = addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(5);
        assertThat(invoker.getToughnessModifier()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent invoker = addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(0);
        assertThat(invoker.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without eight mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Repeated activations stack and affect only the activating Invoker")
    void repeatedActivationsOnlyBoostTheirSource() {
        Permanent invoker = addReadyInvoker();
        Permanent otherInvoker = addReadyInvoker();
        Permanent opposingInvoker = addCreatureReady(player2, new OranRiefInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(10);
        assertThat(invoker.getToughnessModifier()).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isTrue();
        for (Permanent unaffected : new Permanent[]{otherInvoker, opposingInvoker}) {
            assertThat(unaffected.getPowerModifier()).isZero();
            assertThat(unaffected.getToughnessModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Test
    @DisplayName("A tapped, summoning-sick Invoker can activate its ability using colored mana")
    void tappedSummoningSickInvokerCanActivate() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new OranRiefInvoker());
        invoker.setSummoningSick(true);
        invoker.tap();
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(invoker.getPowerModifier()).isEqualTo(5);
        assertThat(invoker.getToughnessModifier()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isTrue();
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An activation does not boost a new permanent after its source leaves")
    void departedSourceDoesNotBoostReplacement() {
        Permanent invoker = addReadyInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(invoker);
        gd.playerHands.get(player1.getId()).add(invoker.getCard());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new OranRiefInvoker());
        harness.passBothPriorities();

        assertThat(replacement.getPowerModifier()).isZero();
        assertThat(replacement.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyInvoker() {
        return addCreatureReady(player1, new OranRiefInvoker());
    }
}
