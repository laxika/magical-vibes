package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrestleTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration targets Trestle Troll and puts the ability on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent troll = addTrestleTrollReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        StackEntry entry = gameData.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(troll.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addTrestleTrollReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Trestle Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without all required mana")
    void cannotActivateWithoutEnoughMana() {
        addTrestleTrollReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addTrestleTrollReady(Player player) {
        Permanent permanent = new Permanent(new TrestleTroll());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
