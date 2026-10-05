package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LathrilBladeOfTheElves.class, ElvishWarrior.class})
class LathrilBladeOfTheElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates that many Elf Warrior tokens")
    void combatDamageCreatesTokens() {
        Permanent lathril = addCreatureReady(player1, new LathrilBladeOfTheElves());
        lathril.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2);
    }

    @Test
    @DisplayName("Tapping Lathril and ten untapped Elves drains opponents and gains life")
    void tapsTenElvesForLifeSwing() {
        Permanent lathril = addCreatureReady(player1, new LathrilBladeOfTheElves());
        for (int i = 0; i < 10; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(lathril.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The activated ability cannot be paid with fewer than ten other untapped Elves")
    void cannotActivateWithoutTenOtherElves() {
        Permanent lathril = addCreatureReady(player1, new LathrilBladeOfTheElves());
        for (int i = 0; i < 9; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lathril.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Newly created Elf tokens can pay the ten-Elf cost")
    void newlyCreatedTokensCanPayCost() {
        Permanent lathril = addCreatureReady(player1, new LathrilBladeOfTheElves());
        lathril.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);
        lathril.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(10);
        harness.assertLife(player2, 10);
        lathril.setAttacking(false);
        lathril.setTapped(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lathril.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Elf Warrior")).allMatch(Permanent::isTapped);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Summoning-sick Lathril cannot activate its tap ability")
    void summoningSickLathrilCannotActivate() {
        Permanent lathril = harness.addToBattlefieldAndReturn(player1, new LathrilBladeOfTheElves());
        lathril.setSummoningSick(true);
        for (int i = 0; i < 10; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Already tapped Elves cannot pay the ten-Elf cost")
    void tappedElvesCannotPayCost() {
        Permanent lathril = addCreatureReady(player1, new LathrilBladeOfTheElves());
        for (int i = 0; i < 9; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }
        Permanent tappedElf = addCreatureReady(player1, new ElvishWarrior());
        tappedElf.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lathril.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Elvish Warrior")).filteredOn(Permanent::isTapped).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Elves cannot pay the ten-Elf cost")
    void opponentsElvesCannotPayCost() {
        Permanent lathril = addCreatureReady(player1, new LathrilBladeOfTheElves());
        for (int i = 0; i < 9; i++) {
            addCreatureReady(player1, new ElvishWarrior());
        }
        Permanent opponentElf = addCreatureReady(player2, new ElvishWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lathril.isTapped()).isFalse();
        assertThat(opponentElf.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Elvish Warrior")).noneMatch(Permanent::isTapped);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
