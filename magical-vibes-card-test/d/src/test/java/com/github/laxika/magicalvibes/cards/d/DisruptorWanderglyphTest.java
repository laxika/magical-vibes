package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IronpawAspirant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptorWanderglyph.class, IronpawAspirant.class})
class DisruptorWanderglyphTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles a targeted card from the defending player's graveyard")
    void attackExilesDefendingPlayerGraveyardCard() {
        addReadyAttacker();
        Card graveyardCard = new IronpawAspirant();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
    }

    @Test
    @DisplayName("A card in the attacker's own graveyard is not a legal target")
    void ownGraveyardCardNotTargetable() {
        addReadyAttacker();
        Card ownCard = new IronpawAspirant();
        Card opponentCard = new IronpawAspirant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCard)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(opponentCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(ownCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ownCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentCard.getId()));
    }

    @Test
    @DisplayName("An empty defending graveyard produces no target choice")
    void emptyDefendingGraveyardNoChoice() {
        addReadyAttacker();
        Card ownCard = new IronpawAspirant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
    }

    private Permanent addReadyAttacker() {
        Permanent wanderglyph = harness.addToBattlefieldAndReturn(player1, new DisruptorWanderglyph());
        wanderglyph.setSummoningSick(false);
        return wanderglyph;
    }

    @Test
    @DisplayName("Only the chosen graveyard card is exiled")
    void exilesOnlyChosenCard() {
        addReadyAttacker();
        Card chosen = new IronpawAspirant();
        Card other = new DisruptorWanderglyph();
        harness.setGraveyard(player2, new ArrayList<>(List.of(chosen, other)));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    @Test
    @DisplayName("A legal target must be chosen when an opponent's graveyard is nonempty")
    void cannotDeclineMandatoryTarget() {
        addReadyAttacker();
        Card chosen = new IronpawAspirant();
        harness.setGraveyard(player2, List.of(chosen));

        declareAttack();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not exiled and no replacement is chosen")
    void targetLeavingGraveyardIsNotExiled() {
        addReadyAttacker();
        Card chosen = new IronpawAspirant();
        Card other = new DisruptorWanderglyph();
        harness.setGraveyard(player2, new ArrayList<>(List.of(chosen, other)));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        gd.playerGraveyards.get(player2.getId()).remove(chosen);
        harness.setHand(player2, List.of(chosen));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player2.getId())).contains(chosen);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves after Wanderglyph leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent source = addReadyAttacker();
        Card chosen = new IronpawAspirant();
        harness.setGraveyard(player2, new ArrayList<>(List.of(chosen)));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, List.of(source.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Attacking allows targeting a card in a nondefending opponent's graveyard")
    void canTargetNondefendingOpponentGraveyard() {
        UUID opponentId = UUID.randomUUID();
        Player opponent = new Player(opponentId, "Charlie");
        gd.playerIds.add(opponentId);
        gd.orderedPlayerIds.add(opponentId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(opponentId, "Charlie");
        gd.playerDecks.put(opponentId, new ArrayList<>());
        gd.playerHands.put(opponentId, new ArrayList<>());
        gd.playerBattlefields.put(opponentId, new ArrayList<>());
        gd.playerGraveyards.put(opponentId, new ArrayList<>());
        gd.playerCommandZones.put(opponentId, new ArrayList<>());
        gd.playerManaPools.put(opponentId, new ManaPool());
        gd.playerLifeTotals.put(opponentId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), opponentId, "Charlie");
        addReadyAttacker();
        Card defendingCard = new IronpawAspirant();
        Card otherOpponentCard = new DisruptorWanderglyph();
        harness.setGraveyard(player2, List.of(defendingCard));
        harness.setGraveyard(opponent, List.of(otherOpponentCard));

        declareAttack();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(defendingCard.getId(), otherOpponentCard.getId());
    }
}
