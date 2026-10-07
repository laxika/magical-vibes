package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxsDecree.class, Shock.class, GrizzlyBears.class})
class SphinxsDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("The restriction does not apply before the opponent's next turn")
    void restrictionStartsOnOpponentsNextTurn() {
        castSphinxsDecree();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The opponent cannot cast instant or sorcery spells during their next turn")
    void opponentCannotCastInstantOrSorceryDuringNextTurn() {
        castSphinxsDecree();
        advanceToNextTurn(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The restriction expires after the opponent's next turn")
    void restrictionExpiresAfterOpponentsNextTurn() {
        castSphinxsDecree();
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The opponent cannot cast a sorcery during their next turn")
    void opponentCannotCastSorceryDuringNextTurn() {
        castSphinxsDecree();
        advanceToNextTurn(player1);

        harness.setHand(player2, List.of(new SphinxsDecree()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Sphinx's Decree");
    }

    @Test
    @DisplayName("The caster can cast instants during the opponent's restricted turn")
    void casterIsNotRestrictedDuringOpponentsTurn() {
        castSphinxsDecree();
        advanceToNextTurn(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("The opponent can cast instants immediately after their restricted turn")
    void restrictionExpiresBeforeCastersNextMainPhase() {
        castSphinxsDecree();
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Shock");
    }

    private void castSphinxsDecree() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SphinxsDecree()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void advanceToNextTurn(Player currentPlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(currentPlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextPlayer = currentPlayer.equals(player1) ? player2 : player1;
        harness.passUntil(nextPlayer, TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
