package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalaGedThief.class, GrizzlyBears.class, SeaGateLoremaster.class})
class BalaGedThiefTest extends BaseCardTest {

    @Test
    @DisplayName("The Thief's own entry makes the target reveal one card and discard it")
    void ownEntryTriggersForOneAlly() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.castFromHand(player1, new BalaGedThief(), "{3}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Another Ally triggers the ability and increases the reveal count")
    void anotherAllyTriggersAndIncreasesCount() {
        harness.addToBattlefield(player1, new BalaGedThief());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        harness.castFromHand(player1, new SeaGateLoremaster(), "{4}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        assertThat(activeChoice().revealedCardIds()).hasSize(2);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A non-Ally creature does not trigger the ability")
    void nonAllyDoesNotTrigger() {
        harness.addToBattlefield(player1, new BalaGedThief());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveal"));
    }

    @Test
    void emptyHandRequiresNoChoice() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new BalaGedThief(), "{3}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(activeChoice()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetControllerAndDiscardFromOwnHand() {
        harness.castFromHand(player1, new BalaGedThief(), "{3}{B}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SeaGateLoremaster()));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Sea Gate Loremaster");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void revealsWholeSmallerHandAndDiscardsOnlyChosenCard() {
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        var chosen = new SeaGateLoremaster();
        var kept = new BalaGedThief();
        harness.setHand(player2, List.of(kept, chosen));
        harness.castFromHand(player1, new BalaGedThief(), "{3}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().revealedCardIds()).containsExactly(kept.getId(), chosen.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen);
    }

    @Test
    void opposingAllyDoesNotTrigger() {
        harness.addToBattlefield(player1, new BalaGedThief());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SeaGateLoremaster(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Sea Gate Loremaster");
    }

    @Test
    void noCardsAreRevealedWhenNoAlliesRemainAtResolution() {
        var kept = new SeaGateLoremaster();
        harness.setHand(player2, List.of(kept));
        harness.castFromHand(player1, new BalaGedThief(), "{3}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(activeChoice()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsAlliesAtResolutionAndIgnoresOpposingAllies() {
        harness.addToBattlefield(player2, new SeaGateLoremaster());
        harness.setHand(player2, List.of(new SeaGateLoremaster(), new SeaGateLoremaster(), new BalaGedThief()));
        harness.castFromHand(player1, new BalaGedThief(), "{3}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player1, new SeaGateLoremaster());
        harness.passBothPriorities();

        assertThat(activeChoice().revealStage()).isTrue();
        assertThat(activeChoice().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Bala Ged Thief");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }
}
