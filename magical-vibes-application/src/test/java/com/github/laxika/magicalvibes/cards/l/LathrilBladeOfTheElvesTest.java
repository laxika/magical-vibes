package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        tapElvesExcept(lathril, 10);
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

    private void tapElvesExcept(Permanent excluded, int count) {
        List<Permanent> elves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != excluded)
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELF))
                .limit(count)
                .toList();
        for (Permanent elf : elves) {
            harness.handlePermanentChosen(player1, elf.getId());
        }
    }
}
