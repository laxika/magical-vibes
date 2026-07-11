package com.github.laxika.magicalvibes.cards.g;

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

class GhostlyChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {1}{B} gives +1/+1")
    void activatingGivesBoost() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent changeling = findByName(gd, player1, "Ghostly Changeling");
        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(changeling.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly to stack the boost")
    void boostStacks() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent changeling = findByName(gd, player1, "Ghostly Changeling");
        assertThat(changeling.getPowerModifier()).isEqualTo(2);
        assertThat(changeling.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability requires mana to activate")
    void abilityRequiresMana() {
        addChangelingReady(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent changeling = findByName(gd, player1, "Ghostly Changeling");
        assertThat(changeling.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(0);
        assertThat(changeling.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addChangelingReady(Player player) {
        GhostlyChangeling card = new GhostlyChangeling();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findByName(GameData gd, Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
