package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmorerGuildmage.class, Forest.class})
class ArmorerGuildmageTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({"0, BLACK, 2, 1", "1, GREEN, 1, 2"})
    void canTargetItself(int abilityIndex, ManaColor manaColor, int power, int toughness) {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, manaColor, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, guildmage.getId());
        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(toughness);
    }

    @ParameterizedTest
    @CsvSource({"0, BLACK", "1, GREEN"})
    void cannotActivateWhileSummoningSick(int abilityIndex, ManaColor manaColor) {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new ArmorerGuildmage());
        guildmage.setSummoningSick(true);
        harness.addMana(player1, manaColor, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"0, BLACK", "1, GREEN"})
    void cannotActivateWhileTapped(int abilityIndex, ManaColor manaColor) {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        guildmage.setTapped(true);
        harness.addMana(player1, manaColor, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN", "1, BLACK"})
    void cannotPayWithWrongColor(int abilityIndex, ManaColor wrongColor) {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, wrongColor, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(wrongColor)).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"0, BLACK, 2, 1", "1, GREEN, 1, 2"})
    void abilityResolvesAfterSourceLeavesBattlefield(int abilityIndex, ManaColor manaColor, int power, int toughness) {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player2, new ArmorerGuildmage());
        harness.addMana(player1, manaColor, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(guildmage);
        gd.playerGraveyards.get(player1.getId()).add(guildmage.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("{B}, {T}: target creature gets +1/+0 until end of turn")
    void boostsPower() {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player2, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("{G}, {T}: target creature gets +0/+1 until end of turn")
    void boostsToughness() {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The +0/+1 boost wears off at end of turn")
    void toughnessBoostWearsOff() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power boost cannot target a noncreature permanent")
    void powerBoostRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The toughness boost cannot target a noncreature permanent")
    void toughnessBoostRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
