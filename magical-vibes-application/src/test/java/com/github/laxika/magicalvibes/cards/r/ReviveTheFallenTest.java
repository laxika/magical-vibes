package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FendeepSummoner;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.p.PullingTeeth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReviveTheFallen.class, PricklyBoggart.class, FendeepSummoner.class, PullingTeeth.class})
class ReviveTheFallenTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void keepClashCardsOnTop() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            var player = scry.playerId().equals(player1.getId()) ? player1 : player2;
            gs.handleInteractionAnswer(gd, player,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
    }

    @Test
    @DisplayName("The creature is returned before players make their clash placement choices")
    void returnsCreatureBeforeClashChoices() {
        harness.setLibrary(player1, List.of(new FendeepSummoner()));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));
        Card target = new PricklyBoggart();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
        keepClashCardsOnTop();
        harness.assertInHand(player1, "Revive the Fallen");
    }

    @Test
    @DisplayName("An illegal sole target prevents the clash and the spell's return")
    void illegalTargetPreventsClash() {
        Card top = new FendeepSummoner();
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));
        Card target = new PricklyBoggart();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gameLogContains("clashes: reveals")).isFalse();
        harness.assertInGraveyard(player1, "Revive the Fallen");
        harness.assertNotInHand(player1, "Revive the Fallen");
    }

    @Test
    @DisplayName("Resolving returns the targeted creature card from a graveyard to its owner's hand")
    void returnsTargetedCreatureToHand() {
        // Player1 loses the clash (Prickly Boggart MV 1 < Fendeep Summoner MV 5) so only the return matters here.
        harness.setLibrary(player1, List.of(new PricklyBoggart()));
        harness.setLibrary(player2, List.of(new FendeepSummoner()));

        Card target = new PricklyBoggart();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        keepClashCardsOnTop();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Can target a creature card in an opponent's graveyard, returning it to that owner's hand")
    void returnsCreatureFromOpponentGraveyard() {
        harness.setLibrary(player1, List.of(new PricklyBoggart()));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));

        Card target = new PricklyBoggart();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        keepClashCardsOnTop();

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Winning the clash returns Revive the Fallen to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Fendeep Summoner MV 5 > Prickly Boggart MV 1 â†’ player1 wins the clash.
        harness.setLibrary(player1, List.of(new FendeepSummoner()));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));

        Card target = new PricklyBoggart();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        keepClashCardsOnTop();

        harness.assertInHand(player1, "Revive the Fallen");
        harness.assertNotInGraveyard(player1, "Revive the Fallen");
    }

    @Test
    @DisplayName("Losing the clash sends Revive the Fallen to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        harness.setLibrary(player1, List.of(new PricklyBoggart()));
        harness.setLibrary(player2, List.of(new FendeepSummoner()));

        Card target = new PricklyBoggart();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Revive the Fallen");
        harness.assertNotInHand(player1, "Revive the Fallen");
    }

    @Test
    @DisplayName("A tied clash does not return Revive the Fallen to its owner's hand")
    void tiedClashDoesNotReturnSpellToHand() {
        harness.setLibrary(player1, List.of(new PricklyBoggart()));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));

        Card target = new PricklyBoggart();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        keepClashCardsOnTop();

        harness.assertInGraveyard(player1, "Revive the Fallen");
        harness.assertNotInHand(player1, "Revive the Fallen");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in a graveyard")
    void cannotTargetNonCreature() {
        Card nonCreature = new PullingTeeth();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bottoming the revealed cards does not change the clash winner")
    void bottomingRevealedCardsPreservesWin() {
        Card ownTop = new FendeepSummoner();
        Card ownNext = new PricklyBoggart();
        Card opponentTop = new PricklyBoggart();
        Card opponentNext = new FendeepSummoner();
        harness.setLibrary(player1, List.of(ownTop, ownNext));
        harness.setLibrary(player2, List.of(opponentTop, opponentNext));
        Card target = new PricklyBoggart();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ReviveTheFallen()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownNext, ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext, opponentTop);
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        harness.assertInHand(player1, "Revive the Fallen");
        harness.assertNotInGraveyard(player1, "Revive the Fallen");
    }
}
