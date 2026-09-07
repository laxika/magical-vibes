package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeetleformMageTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants +2/+2 and flying until end of turn")
    void abilityBoostsAndGrantsFlying() {
        Permanent mage = addReadyMage(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated only once each turn")
    void abilityOncePerTurn() {
        addReadyMage(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void effectsWearOff() {
        Permanent mage = addReadyMage(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mage, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyMage(Player player) {
        Permanent perm = new Permanent(new BeetleformMage());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
