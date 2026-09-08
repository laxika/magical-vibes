package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EscapeRoutesTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns a white creature you control to its owner's hand")
    void returnsWhiteCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertInHand(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("Ability returns a black creature you control to its owner's hand")
    void returnsBlackCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
        harness.assertInHand(player1, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Cannot target a creature of another color")
    void cannotTargetCreatureOfAnotherColor() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or black creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's white creature")
    void cannotTargetOpponentsCreature() {
        addEscapeRoutes(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a white or black creature you control");
    }

    private void addEscapeRoutes(Player player) {
        harness.addToBattlefield(player, new EscapeRoutes());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
