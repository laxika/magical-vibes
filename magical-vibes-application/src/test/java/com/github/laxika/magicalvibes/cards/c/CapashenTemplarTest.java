package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CapashenTemplar.class)
class CapashenTemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Capashen Templar +0/+1")
    void resolvingAbilityBoostsToughness() {
        Permanent templar = addCreatureReady(player1, new CapashenTemplar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, templar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, templar)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple activations accumulate")
    void canActivateMultipleTimes() {
        Permanent templar = addCreatureReady(player1, new CapashenTemplar());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, templar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, templar)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent templar = addCreatureReady(player1, new CapashenTemplar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, templar)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, templar)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can be activated with summoning sickness and does not tap Capashen Templar")
    void canActivateWithoutTappingWithSummoningSickness() {
        Permanent templar = harness.addToBattlefieldAndReturn(player1, new CapashenTemplar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(templar.isTapped()).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, templar)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each activation boosts only the Capashen Templar whose ability resolved")
    void boostAffectsOnlySource() {
        Permanent firstTemplar = addCreatureReady(player1, new CapashenTemplar());
        Permanent secondTemplar = addCreatureReady(player1, new CapashenTemplar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, firstTemplar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondTemplar)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability fizzles if Capashen Templar leaves before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCreatureReady(player1, new CapashenTemplar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without white mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new CapashenTemplar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

}
