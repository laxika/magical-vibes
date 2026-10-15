package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaperGuildmage.class, Island.class})
class ShaperGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: target creature gains first strike until end of turn")
    void grantsFirstStrike() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player1, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("{B}, {T}: target creature gets +1/+0 until end of turn")
    void boostsTargetCreature() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player1, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first-strike ability cannot be paid with non-white mana")
    void firstStrikeRequiresWhiteMana() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The tap cost prevents activating another ability while tapped")
    void cannotActivateAnotherAbilityWhileTapped() {
        Permanent source = addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The abilities cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either ability can target the Guildmage itself")
    void canTargetItself(int abilityIndex) {
        Permanent source = addCreatureReady(player1, new ShaperGuildmage());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.WHITE : ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, source.getId());
        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isEqualTo(abilityIndex == 0);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(abilityIndex == 1 ? 2 : 1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Summoning sickness prevents paying either ability's tap cost")
    void cannotActivateWithSummoningSickness(int abilityIndex) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, abilityIndex == 0 ? ManaColor.WHITE : ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost ability cannot be paid with non-black mana")
    void boostRequiresBlackMana() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The boost ability cannot target a noncreature permanent")
    void boostCannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Boosts from two Guildmages accumulate on the same creature")
    void boostsAccumulate() {
        addCreatureReady(player1, new ShaperGuildmage());
        addCreatureReady(player1, new ShaperGuildmage());
        Permanent target = addCreatureReady(player2, new ShaperGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }
}
