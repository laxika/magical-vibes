package com.github.laxika.magicalvibes.cards.m;

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

class MinersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +1/+0 and trample")
    void abilityGrantsBoostAndTrample() {
        Permanent bane = addBane(player1);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bane)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Activating twice stacks the boosts")
    void activatingTwiceStacksBoosts() {
        Permanent bane = addBane(player1);
        addCost(player1);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bane = addBane(player1);
        addCost(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bane)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bane, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addBane(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addBane(Player player) {
        Permanent perm = new Permanent(new MinersBane());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addCost(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
