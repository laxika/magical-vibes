package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrilledSandwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +2/+2 until end of turn")
    void resolvingBoosts() {
        addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sandwalla = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can only activate once each turn")
    void onlyOncePerTurn() {
        addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addSandwallaReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sandwalla = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(sandwalla.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sandwalla.getEffectivePower()).isEqualTo(1);
        assertThat(sandwalla.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addSandwallaReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addSandwallaReady(Player player) {
        FrilledSandwalla card = new FrilledSandwalla();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
