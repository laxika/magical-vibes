package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrownerInitiateTest extends BaseCardTest {

    private static void trimDeck(List<Card> deck) {
        while (deck.size() > 10) {
            deck.removeFirst();
        }
    }

    @Test
    @DisplayName("Blue spell cast, pay {1}, target opponent mills two cards")
    void blueSpellPayMillsOpponent() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);      // Fugitive Wizard's {U}
        harness.addMana(player1, ManaColor.COLORLESS, 1); // the {1} to pay

        List<Card> deck = gd.playerDecks.get(player2.getId());
        trimDeck(deck);
        int deckSizeBefore = deck.size();

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the payment mills nothing")
    void declineNoMill() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Drowner Initiate"));

        harness.passBothPriorities(); // resolve creature spell
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player casting a blue spell triggers even for the opponent")
    void opponentBlueSpellTriggers() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        trimDeck(deck);
        int deckSizeBefore = deck.size();

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, player1.getId()); // target self
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Non-blue spell does not trigger")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
