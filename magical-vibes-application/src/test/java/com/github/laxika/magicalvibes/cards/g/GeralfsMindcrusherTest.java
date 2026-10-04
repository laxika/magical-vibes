package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GeralfsMindcrusher.class, LightningBolt.class, GrafdiggersCage.class})
class GeralfsMindcrusherTest extends BaseCardTest {

    private void castMindcrusher(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GeralfsMindcrusher()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0, targetPlayerId);
    }


    @Test
    @DisplayName("Resolving puts Geralf's Mindcrusher on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castMindcrusher(player2.getId());
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Geralf's Mindcrusher");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }


    @Test
    @DisplayName("ETB trigger mills five cards from target player's library")
    void etbMillsFiveCards() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castMindcrusher(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        castMindcrusher(player1.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than five")
    void millsOnlyRemainingWhenLibrarySmall() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 3) {
            deck.removeFirst();
        }

        castMindcrusher(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }


    @Test
    @DisplayName("Undying returns Geralf's Mindcrusher with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new GeralfsMindcrusher());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Two bolts (3 + 3 = 6) to kill the 5/5.
        UUID mindcrusherId = harness.getPermanentId(player1, "Geralf's Mindcrusher");
        harness.castInstant(player1, 0, mindcrusherId);
        resolveAllTriggers();
        harness.castInstant(player1, 0, mindcrusherId);
        resolveAllTriggers();

        // Undying returned it with a +1/+1 counter and its ETB ability now asks for a target.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        Permanent mindcrusher = findPermanent(player1, "Geralf's Mindcrusher");
        assertThat(mindcrusher).isNotNull();
        assertThat(mindcrusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mindcrusher.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("Undying return re-triggers the ETB, milling the chosen player five cards")
    void undyingReturnRetriggersEtb() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.addToBattlefield(player1, new GeralfsMindcrusher());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID mindcrusherId = harness.getPermanentId(player1, "Geralf's Mindcrusher");
        harness.castInstant(player1, 0, mindcrusherId);
        resolveAllTriggers();
        harness.castInstant(player1, 0, mindcrusherId);
        resolveAllTriggers();

        // Choose the opponent as the target of the returned Mindcrusher's ETB mill.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Undying does not return Geralf's Mindcrusher when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent mindcrusher = harness.addToBattlefieldAndReturn(player1, new GeralfsMindcrusher());
        mindcrusher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // now 6/6
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Two bolts (3 + 3 = 6 damage) to kill the 6/6.
        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Geralf's Mindcrusher");
        harness.assertNotOnBattlefield(player1, "Geralf's Mindcrusher");
    }

    @Test
    @DisplayName("Milling an empty library does not cause a player to lose")
    void emptyLibraryCanBeMilled() {
        harness.setLibrary(player2, List.of());

        castMindcrusher(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents undying from returning Mindcrusher")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Permanent mindcrusher = harness.addToBattlefieldAndReturn(player1, new GeralfsMindcrusher());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Geralf's Mindcrusher");
        harness.assertNotOnBattlefield(player1, "Geralf's Mindcrusher");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Undying is controlled by the creature's controller at death and returns it to its owner")
    void undyingOfOpponentOwnedCreatureUsesControllerAtDeath() {
        GeralfsMindcrusher card = new GeralfsMindcrusher();
        card.setOwnerId(player2.getId());
        Permanent mindcrusher = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, mindcrusher.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, mindcrusher.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Geralf's Mindcrusher");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Geralf's Mindcrusher");
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }
}
