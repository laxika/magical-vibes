package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiverSerpentTest extends BaseCardTest {

    // ===== Attack restriction =====

    @Test
    @DisplayName("River Serpent can attack with five cards in its controller's graveyard")
    void canAttackWithFiveCardsInGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, fiveCards());

        Permanent serpent = new Permanent(new RiverSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(findIndex(player1, serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("River Serpent cannot attack with fewer than five cards in its controller's graveyard")
    void cannotAttackWithFourCardsInGraveyard() {
        List<Card> four = fiveCards();
        four.remove(0);
        harness.setGraveyard(player1, four);

        Permanent serpent = new Permanent(new RiverSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int serpentIndex = findIndex(player1, serpent);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller's own graveyard counts toward the five")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, fiveCards());

        Permanent serpent = new Permanent(new RiverSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int serpentIndex = findIndex(player1, serpent);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cycling =====

    @Test
    @DisplayName("Cycling {U} discards River Serpent and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RiverSerpent()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "River Serpent");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    // ===== Helpers =====

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private int findIndex(Player player, Permanent target) {
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i) == target) return i;
        }
        throw new IllegalStateException("Permanent not found on battlefield");
    }
}
