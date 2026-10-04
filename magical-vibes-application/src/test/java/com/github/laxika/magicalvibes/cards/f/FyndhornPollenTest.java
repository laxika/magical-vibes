package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FyndhornPollen.class, BalduvianBears.class, Opalescence.class})
class FyndhornPollenTest extends BaseCardTest {

    @Test
    @DisplayName("Static ability gives every creature -1/-0")
    void staticDebuffsAllCreatures() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(pollen);

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability stacks another -1/-0 until end of turn")
    void activatedAbilityStacksAndWearsOff() {
        harness.addToBattlefield(player1, new FyndhornPollen());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mine)).isZero();
        assertThat(gqs.getEffectivePower(gd, theirs)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Fyndhorn Pollen")
    void declineSacrificesPollen() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(pollen.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pollen);
        harness.assertInGraveyard(player1, "Fyndhorn Pollen");
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Fyndhorn Pollen")
    void payingUpkeepKeepsPollen() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pollen);
    }

    @Test
    @DisplayName("Cumulative upkeep costs one generic mana per age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());
        pollen.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(pollen.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pollen);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Insufficient mana for cumulative upkeep sacrifices Fyndhorn Pollen")
    void insufficientManaSacrificesPollen() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());
        pollen.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        gd.playerManaPools.get(player1.getId()).addPersistentMana(ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pollen);
        harness.assertInGraveyard(player1, "Fyndhorn Pollen");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fyndhorn Pollen's static debuff includes itself when it becomes a creature")
    void staticDebuffIncludesAnimatedPollen() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());
        harness.addToBattlefield(player2, new Opalescence());

        assertThat(gqs.isCreature(gd, pollen)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pollen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pollen)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activated debuff affects creatures present at resolution but not later arrivals")
    void activatedDebuffLocksInCreaturesAtResolution() {
        harness.addToBattlefield(player1, new FyndhornPollen());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isZero();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("A resolved activated debuff persists after Fyndhorn Pollen is sacrificed")
    void activatedDebuffPersistsWithoutSource() {
        harness.addToBattlefield(player1, new FyndhornPollen());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        advanceToUpkeep(player1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Fyndhorn Pollen");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations stack and can reduce power below zero")
    void repeatedActivationsStack() {
        harness.addToBattlefield(player1, new FyndhornPollen());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during an opponent's upkeep")
    void noUpkeepCostOnOpponentsTurn() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(pollen.getCounterCount(CounterType.AGE)).isZero();
        harness.assertOnBattlefield(player1, "Fyndhorn Pollen");
    }
}
