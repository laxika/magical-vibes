package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flux.class, GrizzlyBears.class, HillGiant.class, Plains.class, Island.class, Mountain.class,
        Forest.class})
class FluxTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards a chosen number and draws that many, then controller draws one")
    void eachPlayerRummagesThenControllerDrawsOne() {
        harness.setHand(player1, List.of(new Flux(), new GrizzlyBears(), new HillGiant()));
        harness.setLibrary(player1, List.of(new Plains(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Flux resolves, active player chooses first (APNAP)

        // Player 1 (active) discards 2 and draws 2.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0); // Grizzly Bears
        harness.handleCardChosen(player1, 0); // Hill Giant

        // Player 2 discards 1 and draws 1.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0); // Grizzly Bears

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        // Player 1 drew its two rummaged cards plus the trailing "Draw a card".
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3)
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Plains", "Island", "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Hill Giant");

        // Player 2 drew its single rummaged card only (no trailing draw).
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1)
                .extracting(c -> c.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        assertThat(gd.discardEventPlayerId).isNull();
        assertThat(gd.eachPlayerRummage.active).isFalse();
    }

    @Test
    @DisplayName("Choosing zero discards nothing and draws nothing, but the controller still draws one")
    void chooseZeroDiscardsNothing() {
        harness.setHand(player1, List.of(new Flux(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HillGiant()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0); // player 1 keeps its hand
        harness.handleXValueChosen(player2, 0); // player 2 keeps its hand

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        // Player 1 kept Grizzly Bears and drew the trailing card.
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Plains");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .doesNotContain("Grizzly Bears"); // only the spent Flux is here

        // Player 2 kept its hand and drew nothing.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("All players choose their discards before any selected card is discarded or drawn")
    void discardsWaitForEveryPlayerChoice() {
        harness.setHand(player1, List.of(new Flux(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Plains(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HillGiant()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        PendingInteraction.XValueChoice nextChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(nextChoice).isNotNull();
        assertThat(nextChoice.playerId()).isEqualTo(player2.getId());
    }

    @Test
    @CardUsed(TamiyoCollectorOfTales.class)
    @DisplayName("Prevented opponent discards do not leave Flux resolution state active")
    void opponentDiscardPreventionDoesNotLeaveStateActive() {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new Flux(), new Flux()));
        harness.setLibrary(player1, List.of(new Plains(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new HillGiant()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Hill Giant");

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice nextChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(nextChoice).isNotNull();
        assertThat(nextChoice.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A player with an empty hand is skipped; the controller still draws one at the end")
    void emptyHandedActivePlayerIsSkipped() {
        harness.setHand(player1, List.of(new Flux()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // player 1 has no cards, so player 2 chooses immediately

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0); // Grizzly Bears

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        // Player 1 only drew the trailing card.
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Plains");

        // Player 2 discarded and drew one.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Discards wait until every player has chosen how many cards to discard")
    void selectedCardsRemainInHandUntilEveryPlayerChooses() {
        harness.setHand(player1, List.of(new Flux(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new HillGiant()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c instanceof GrizzlyBears);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Plains.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
    }
}
