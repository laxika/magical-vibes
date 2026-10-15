package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LedgerShredder.class, GrizzlyBears.class, Mountain.class, Shock.class})
class LedgerShredderTest extends BaseCardTest {

    @Test
    void connivesOnSecondSpellAndAddsCounterForNonlandDiscard() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new LedgerShredder());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName(player1, "Grizzly Bears");

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void connivingWithLandDiscardDoesNotAddCounter() {
        Permanent shredder = addCreatureReady(player1, new LedgerShredder());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName(player1, "Mountain");

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void connivesWhenOpponentCastsTheirSecondSpell() {
        Permanent shredder = addCreatureReady(player1, new LedgerShredder());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName(player1, "Grizzly Bears");

        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotConniveOnFirstOrThirdSpell() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new LedgerShredder());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        discardByName(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachPlayersFirstSpellDoesNotCountAsASecondSpell() {
        harness.addToBattlefield(player1, new LedgerShredder());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void stillDrawsAndDiscardsWhenRemovedBeforeTriggerResolves() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new LedgerShredder());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(shredder);
        harness.setGraveyard(player1, List.of(shredder.getCard()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        discardByName(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForItsOwnCastAsSecondSpell() {
        harness.setHand(player1, List.of(new Shock(), new LedgerShredder()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ledger Shredder");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void currentControllerConnivesAfterControlChangesWithTriggerOnStack() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new LedgerShredder());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(shredder);
        gd.playerBattlefields.get(player2.getId()).add(shredder);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        discardByName(player2, "Grizzly Bears");
        assertThat(shredder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void discardByName(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        List<Card> hand = gd.playerHands.get(player.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player, index);
    }
}
