package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcquisitionsExpert.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class AcquisitionsExpertTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry makes the opponent reveal one card and discard it")
    void ownEntryCountsAsOnePartyMember() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        castAndResolve();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The entry uses the maximum number of distinct party roles")
    void fullPartyMakesOpponentRevealFourCards() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears())));

        castAndResolve();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal.remainingCount()).isEqualTo(4);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 3);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The entry can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new AcquisitionsExpert()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    @Test
    @DisplayName("An empty opposing hand requires no reveal or discard choice")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());

        castAndResolve();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A hand smaller than the party is revealed in full and only one card is discarded")
    void smallHandSkipsOpponentRevealChoice() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player2, List.of(first, second));

        castAndResolve();

        assertThat(activeChoice().revealStage()).isFalse();
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeChoice().revealedCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Duplicate rogues and opposing party members do not increase your party")
    void duplicateAndOpposingRolesDoNotCount() {
        harness.addToBattlefield(player1, new AcquisitionsExpert());
        harness.addToBattlefield(player2, new SoulWarden());
        harness.addToBattlefield(player2, new BoggartBrute());
        harness.addToBattlefield(player2, new FugitiveWizard());
        GrizzlyBears hidden = new GrizzlyBears();
        GrizzlyBears revealed = new GrizzlyBears();
        harness.setHand(player2, List.of(hidden, revealed));

        castAndResolve();

        assertThat(activeChoice().remainingCount()).isEqualTo(1);
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);
        assertThat(activeChoice().revealedCardIds()).containsExactly(revealed.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hidden);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Party size is evaluated on resolution after the Expert has left")
    void noPartyAtResolutionRevealsNothing() {
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));
        harness.setHand(player1, List.of(new AcquisitionsExpert()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Acquisitions Expert");
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new AcquisitionsExpert()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
