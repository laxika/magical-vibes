package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MultiformWonder.class})
class MultiformWonderTest extends BaseCardTest {

    @Test
    void entersWithThreeEnergyCounters() {
        harness.setHand(player1, List.of(new MultiformWonder()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void gainsChosenKeywordUntilEndOfTurn() {
        Permanent wonder = addReadyWonder(player1);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.hasKeyword(gd, wonder, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wonder, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void canChooseFlyingOrVigilance() {
        Permanent wonder = addReadyWonder(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Flying");
        assertThat(gqs.hasKeyword(gd, wonder, Keyword.FLYING)).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");
        assertThat(gqs.hasKeyword(gd, wonder, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void getsChosenPowerAndToughnessBoostUntilEndOfTurn() {
        Permanent wonder = addReadyWonder(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        assertThat(gqs.getEffectivePower(gd, wonder)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wonder)).isEqualTo(1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets -2/+2");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, wonder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wonder)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutEnergy() {
        Permanent wonder = addReadyWonder(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
        assertThat(gqs.getEffectivePower(gd, wonder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wonder)).isEqualTo(3);
    }

    @Test
    void toughnessBoostExpiresAtEndOfTurn() {
        Permanent wonder = addReadyWonder(player1);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets -2/+2");

        assertThat(gqs.getEffectivePower(gd, wonder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wonder)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wonder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wonder)).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void repeatedPowerBoostsPutWonderWithNonpositiveToughnessInGraveyard() {
        addReadyWonder(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        harness.assertNotOnBattlefield(player1, "Multiform Wonder");
        harness.assertInGraveyard(player1, "Multiform Wonder");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void energyIsPaidAtActivationAndKeywordChoicesAreIndependent() {
        Permanent wonder = addReadyWonder(player1);
        Permanent otherWonder = addReadyWonder(player1);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wonder, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Flying");
        resolveAllTriggers();
        harness.handleListChoice(player1, "Vigilance");

        assertThat(gqs.hasKeyword(gd, wonder, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wonder, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherWonder, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherWonder, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void statAbilityCannotActivateWithoutEnergy() {
        addReadyWonder(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent wonder = addReadyWonder(player1);
        wonder.setSummoningSick(true);
        wonder.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets -2/+2");

        assertThat(gqs.hasKeyword(gd, wonder, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wonder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wonder)).isEqualTo(5);
        assertThat(wonder.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    private Permanent addReadyWonder(Player player) {
        return addCreatureReady(player, new MultiformWonder());
    }
}
