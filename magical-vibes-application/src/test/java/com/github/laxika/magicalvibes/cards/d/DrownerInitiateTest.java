package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrownerInitiate.class, BriarberryCohort.class, SafeholdSentry.class})
class DrownerInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Blue spell cast, pay {1}, target opponent mills two cards")
    void blueSpellPayMillsOpponent() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1); // the {1} to pay
        harness.castFromHand(player1, new BriarberryCohort(), "{1}{U}");

        List<Card> deck = gd.playerDecks.get(player2.getId());
        harness.setLibrary(player2, deck.subList(Math.max(0, deck.size() - 10), deck.size()));
        deck = gd.playerDecks.get(player2.getId());
        int deckSizeBefore = deck.size();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the payment mills nothing")
    void declineNoMill() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new BriarberryCohort(), "{1}{U}");

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
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

        harness.castFromHand(player2, new BriarberryCohort(), "{1}{U}");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        harness.setLibrary(player1, deck.subList(Math.max(0, deck.size() - 10), deck.size()));
        deck = gd.playerDecks.get(player1.getId());
        int deckSizeBefore = deck.size();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Non-blue spell does not trigger")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.castFromHand(player1, new SafeholdSentry(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Accepting without enough mana mills nothing")
    void insufficientManaNoMill() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        List<Card> deck = gd.playerDecks.get(player2.getId());
        int deckSizeBefore = deck.size();

        harness.castFromHand(player1, new BriarberryCohort(), "{1}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Payment can use mana obtained after the trigger is put on the stack")
    void manaAddedAfterTriggerCanPay() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        harness.castFromHand(player1, new BriarberryCohort(), "{1}{U}");
        harness.handlePermanentChosen(player1, player2.getId());
        int before = gd.playerDecks.get(player2.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A library with one card mills only that card")
    void millsRemainingCardInShortLibrary() {
        harness.addToBattlefield(player1, new DrownerInitiate());
        Card remaining = new SafeholdSentry();
        harness.setLibrary(player2, List.of(remaining));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new BriarberryCohort(), "{1}{U}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
    }
}
