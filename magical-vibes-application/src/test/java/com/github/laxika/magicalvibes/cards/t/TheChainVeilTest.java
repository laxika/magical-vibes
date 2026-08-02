package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheChainVeilTest extends BaseCardTest {

    @Test
    @DisplayName("Controller loses 2 life at end step when no loyalty ability was activated")
    void losesLifeWhenNoLoyaltyAbilityActivated() {
        harness.addToBattlefield(player1, new TheChainVeil());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("No life loss when a loyalty ability was activated this turn")
    void noLifeLossAfterLoyaltyActivation() {
        harness.addToBattlefield(player1, new TheChainVeil());
        addReadyJace(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Trigger only fires on the controller's end step")
    void noLifeLossOnOpponentEndStep() {
        harness.addToBattlefield(player1, new TheChainVeil());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Activated ability lets a planeswalker use a second loyalty ability the same turn")
    void grantsExtraLoyaltyActivation() {
        harness.addToBattlefield(player1, new TheChainVeil());
        Permanent jace = addReadyJace(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("Extra activation is only one per planeswalker, not unlimited")
    void extraActivationIsOnlyOne() {
        harness.addToBattlefield(player1, new TheChainVeil());
        addReadyJace(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    private Permanent addReadyJace(Player player) {
        JaceBeleren card = new JaceBeleren();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
