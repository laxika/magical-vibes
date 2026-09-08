package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordSkitterSewerKing.class, RuinRat.class, GrizzlyBears.class, Shock.class})
class LordSkitterSewerKingTest extends BaseCardTest {

    @Test
    @DisplayName("Another Rat entering exiles up to one target card from an opponent's graveyard")
    void ratEnteringExilesOpponentGraveyardCard() {
        Card opponentCard = new GrizzlyBears();
        Card ownCard = new Shock();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new LordSkitterSewerKing());

        castRuinRat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("The Rat trigger may choose zero cards")
    void ratTriggerMayChooseZeroCards() {
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new LordSkitterSewerKing());

        castRuinRat();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A non-Rat entering does not trigger the graveyard exile")
    void nonRatEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new LordSkitterSewerKing());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Beginning of combat creates a Rat token that cannot block")
    void beginningOfCombatCreatesNonBlockingRatToken() {
        harness.addToBattlefield(player1, new LordSkitterSewerKing());
        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Rat").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(bls.canBlock(gd, token)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat trigger does not fire during an opponent's combat")
    void beginningOfCombatDoesNotFireDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new LordSkitterSewerKing());
        advanceToCombat(player2);

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castRuinRat() {
        harness.setHand(player1, List.of(new RuinRat()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
