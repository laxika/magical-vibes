package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IllGottenInheritanceTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to each opponent and gains 1 life")
    void upkeepTriggerDrainsOpponent() {
        harness.addToBattlefield(player1, new IllGottenInheritance());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrifice ability deals 4 damage to target opponent and gains 4 life")
    void sacrificeAbilityDrainsOpponent() {
        harness.addToBattlefield(player1, new IllGottenInheritance());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Ill-Gotten Inheritance");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target its controller")
    void sacrificeAbilityCannotTargetController() {
        harness.addToBattlefield(player1, new IllGottenInheritance());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a permanent")
    void sacrificeAbilityCannotTargetPermanent() {
        harness.addToBattlefield(player1, new IllGottenInheritance());
        var land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }
}
