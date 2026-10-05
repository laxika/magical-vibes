package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NayaBattlemage.class, CylianElf.class})
class NayaBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives target creature +2/+0 until end of turn")
    void redAbilityBoostsTargetCreature() {
        setup();
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Cylian Elf");
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Red ability boost wears off at end of turn")
    void redBoostWearsOff() {
        setup();
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cylian Elf").getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("White ability taps target creature")
    void whiteAbilityTapsTargetCreature() {
        setup();
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cylian Elf").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the red ability without red mana")
    void cannotActivateWithoutMana() {
        setup();
        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void canTargetOpponentCreature(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new NayaBattlemage());
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(ability == 0 ? 2 : 0);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.isTapped()).isEqualTo(ability == 1);
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void canTargetItself(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new NayaBattlemage());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(source.getPowerModifier()).isEqualTo(ability == 0 ? 2 : 0);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void summoningSicknessPreventsActivation(int ability, ManaColor mana) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NayaBattlemage());
        source.setSummoningSick(true);
        harness.addMana(player1, mana, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ability, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void tappedSourceCannotActivate(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new NayaBattlemage());
        source.tap();
        harness.addMana(player1, mana, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ability, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"0, WHITE", "1, RED"})
    void otherAbilityManaCannotPayCost(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new NayaBattlemage());
        harness.addMana(player1, mana, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ability, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void returningTargetIsANewObject(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new NayaBattlemage());
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        harness.passBothPriorities();

        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, RED", "1, WHITE"})
    void resolvesAfterSourceLeavesBattlefield(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new NayaBattlemage());
        Permanent target = addCreatureReady(player2, new CylianElf());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(ability == 0 ? 2 : 0);
        assertThat(target.isTapped()).isEqualTo(ability == 1);
    }

    @Test
    void whiteAbilityCanTargetAlreadyTappedCreature() {
        addCreatureReady(player1, new NayaBattlemage());
        Permanent target = addCreatureReady(player2, new CylianElf());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void setup() {
        addCreatureReady(player1, new NayaBattlemage());
        harness.addToBattlefield(player1, new CylianElf());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
