package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Duskwielder.class})
class DuskwielderTest extends BaseCardTest {

    @Test
    @DisplayName("Boast makes target opponent lose 1 life and its controller gain 1 life")
    void boastDrainsTargetOpponent() {
        Permanent duskwielder = addCreatureReady(player1, new Duskwielder());
        duskwielder.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Boast requires Duskwielder to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new Duskwielder());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent duskwielder = addCreatureReady(player1, new Duskwielder());
        duskwielder.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast cannot target its controller")
    void boastCannotTargetController() {
        Permanent duskwielder = addCreatureReady(player1, new Duskwielder());
        duskwielder.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
    @Test
    @DisplayName("Boast can be activated while Duskwielder is tapped and still attacking")
    void boastDuringCombatAfterDeclaringAttackers() {
        Permanent duskwielder = addCreatureReady(player1, new Duskwielder());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(duskwielder.isTapped()).isTrue();
            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The once-per-turn limit applies before the first activation resolves")
    void boastCannotBeActivatedAgainWhileOnStack() {
        Permanent duskwielder = addCreatureReady(player1, new Duskwielder());
        duskwielder.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each Duskwielder has its own once-per-turn boast allowance")
    void separateCopiesCanEachBoast() {
        Permanent first = addCreatureReady(player1, new Duskwielder());
        Permanent second = addCreatureReady(player1, new Duskwielder());
        first.setAttackedThisTurn(true);
        second.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Boast drains the opponent relative to the ability controller")
    void boastWorksForOtherPlayer() {
        Permanent duskwielder = addCreatureReady(player2, new Duskwielder());
        duskwielder.setAttackedThisTurn(true);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
    }
}
