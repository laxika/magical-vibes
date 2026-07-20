package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GalvanicJuggernautTest extends BaseCardTest {

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Galvanic Juggernaut does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.tap();

        advanceToNextTurn(player2);

        assertThat(juggernaut.isTapped()).isTrue();
    }

    // ===== Untaps when another creature dies =====

    @Test
    @DisplayName("Galvanic Juggernaut untaps when another creature dies")
    void untapsWhenAnotherCreatureDies() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → Grizzly Bears dies → trigger
        harness.passBothPriorities(); // resolve mandatory untap trigger

        assertThat(juggernaut.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Galvanic Juggernaut does not untap on its own death (only 'another creature')")
    void staysTappedWhenNoOtherCreatureDies() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Opponent controls no creatures, so nothing dies and no trigger fires.
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(juggernaut.isTapped()).isTrue();
    }

    // ===== Attacks each combat if able =====

    @Test
    @DisplayName("Declaring no attackers while Galvanic Juggernaut can attack throws 'must attack'")
    void mustAttackWhenAble() {
        addReadyJuggernaut(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    // ===== Helpers =====

    private Permanent addReadyJuggernaut(Player player) {
        Permanent perm = new Permanent(new GalvanicJuggernaut());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
