package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FyndhornPollenTest extends BaseCardTest {

    @Test
    @DisplayName("Static ability gives every creature -1/-0")
    void staticDebuffsAllCreatures() {
        Permanent pollen = harness.addToBattlefieldAndReturn(player1, new FyndhornPollen());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
}
