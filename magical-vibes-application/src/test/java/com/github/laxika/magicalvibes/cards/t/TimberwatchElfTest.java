package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimberwatchElf.class, LlanowarElves.class, GoblinCohort.class, Forest.class})
class TimberwatchElfTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature by the number of Elves on all battlefields")
    void boostsByElvesOnAllBattlefields() {
        addCreatureReady(player1, new TimberwatchElf());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts Elves when the ability resolves")
    void countsElvesAtResolution() {
        addCreatureReady(player1, new TimberwatchElf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TimberwatchElf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new TimberwatchElf());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself and activate with no mana by tapping")
    void activatesWithoutManaAndCanTargetItself() {
        Permanent elf = addCreatureReady(player1, new TimberwatchElf());

        harness.activateAbility(player1, 0, null, elf.getId());

        assertThat(elf.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate while tapped even with mana available")
    void cannotActivateWhileTapped() {
        Permanent elf = addCreatureReady(player1, new TimberwatchElf());
        elf.tap();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick even with mana available")
    void cannotActivateWhileSummoningSick() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new TimberwatchElf());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The resolved boost does not change when more Elves enter")
    void resolvedBoostIsFixed() {
        addCreatureReady(player1, new TimberwatchElf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
