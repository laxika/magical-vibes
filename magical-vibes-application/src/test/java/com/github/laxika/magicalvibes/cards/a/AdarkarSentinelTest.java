package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdarkarSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Adarkar Sentinel")
    void resolvingAbilityBoostsToughness() {
        addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent sentinel = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sentinel.getEffectivePower()).isEqualTo(3);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(4);
        assertThat(sentinel.getPowerModifier()).isEqualTo(0);
        assertThat(sentinel.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly if mana allows")
    void canActivateMultipleTimes() {
        addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        Permanent sentinel = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(6);
        assertThat(sentinel.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sentinel = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinel.getToughnessModifier()).isEqualTo(0);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addSentinelReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addSentinelReady(Player player) {
        Permanent perm = new Permanent(new AdarkarSentinel());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
