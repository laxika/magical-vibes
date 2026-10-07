package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThranTome.class)
class ThranTomeTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted opponent chooses a revealed card to graveyard, then the controller draws two")
    void opponentChoosesCardThenControllerDrawsTwo() {
        Card top = new ThranTome();
        Card chosen = new ThranTome();
        Card third = new ThranTome();
        Card fourth = new ThranTome();
        harness.setLibrary(player1, List.of(top, chosen, third, fourth));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().playerId()).isEqualTo(player2.getId());
        assertThat(choice.params().targetPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.params().cards()).extracting(Card::getId)
                .containsExactly(top.getId(), chosen.getId(), third.getId());

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(chosen.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(top.getId(), third.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(fourth.getId());
    }

    @Test
    @DisplayName("With fewer than three cards, the opponent chooses from the available cards")
    void usesAvailableCardsWhenLibraryHasFewerThanThree() {
        Card top = new ThranTome();
        Card second = new ThranTome();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(top.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With one card, the only available card is put into the graveyard without a choice")
    void usesOnlyAvailableCardWhenLibraryHasOneCard() {
        Card only = new ThranTome();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(only.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        harness.setLibrary(player1, List.of(new ThranTome(), new ThranTome(), new ThranTome()));
        harness.addToBattlefield(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealsTheOnlyLibraryCardToBothPlayersBeforePuttingItInTheGraveyard() throws Exception {
        Card only = new ThranTome();
        harness.setLibrary(player1, List.of(only));
        harness.addToBattlefield(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch -> events.addAll(batch.events()))) {
            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal reveal
                        && reveal.subjectPlayerId().equals(player1.getId())
                        && reveal.zone() == GameEventFact.RevealZone.LIBRARY)
                .isNotEmpty()
                .allSatisfy(event -> {
                    GameEventFact.PrivateReveal reveal = (GameEventFact.PrivateReveal) event.fact();
                    assertThat(reveal.cards()).extracting(GameEventFact.CardSnapshot::cardId)
                            .containsExactly(only.getId());
                    assertThat(event.audience().playerIds())
                            .containsExactlyInAnyOrder(player1.getId(), player2.getId());
                });
    }

    @Test
    void emptyLibraryDoesNotPromptForAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void activationTapsTheTomeAndCannotBeRepeatedWhileTapped() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(tome.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithOnlyFourMana() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new ThranTome());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tome.isTapped()).isFalse();
    }
}
