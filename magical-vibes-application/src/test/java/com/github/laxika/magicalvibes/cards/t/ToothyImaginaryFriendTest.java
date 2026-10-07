package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PirImaginativeRascal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToothyImaginaryFriend.class, PirImaginativeRascal.class})
class ToothyImaginaryFriendTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Pir")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card pir = new PirImaginativeRascal();
        harness.setLibrary(player2, List.of(pir));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new ToothyImaginaryFriend());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(pir);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Whenever you draw a card, Toothy gets a +1/+1 counter")
    void drawingCardsPutsCountersOnToothy() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PirImaginativeRascal(), new PirImaginativeRascal()));
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, new ToothyImaginaryFriend());

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(toothy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("When Toothy leaves the battlefield, its controller draws for each +1/+1 counter")
    void leavingDrawsForEachPlusOneCounter() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PirImaginativeRascal(), new PirImaginativeRascal(), new PirImaginativeRascal()));
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, new ToothyImaginaryFriend());
        toothy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, toothy));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The target player may decline the partner search")
    void partnerSearchCanBeDeclined() {
        Card pir = new PirImaginativeRascal();
        harness.setLibrary(player2, List.of(pir));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new ToothyImaginaryFriend());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(pir);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's draw does not put a counter on Toothy")
    void opponentsDrawDoesNotPutCountersOnToothy() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new PirImaginativeRascal()));
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, new ToothyImaginaryFriend());

        drawAndResolveTrigger(player2);

        assertThat(toothy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Leaving with no +1/+1 counters draws no cards")
    void leavingWithoutPlusOneCountersDrawsNothing() {
        harness.setHand(player1, List.of());
        Card pir = new PirImaginativeRascal();
        harness.setLibrary(player1, List.of(pir));
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, new ToothyImaginaryFriend());
        toothy.setCounterCount(CounterType.CHARGE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, toothy));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pir);
    }

    @Test
    @DisplayName("Returning Toothy to hand draws using its last counter count")
    void returningToHandDrawsForLastKnownCounters() {
        harness.setHand(player1, List.of());
        Card pir = new PirImaginativeRascal();
        harness.setLibrary(player1, List.of(pir));
        Card toothyCard = new ToothyImaginaryFriend();
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, toothyCard);
        toothy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, toothy));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(toothyCard, pir);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling Toothy also triggers the draw ability")
    void exilingDrawsForLastKnownCounters() {
        harness.setHand(player1, List.of());
        Card pir = new PirImaginativeRascal();
        harness.setLibrary(player1, List.of(pir));
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, new ToothyImaginaryFriend());
        toothy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, toothy));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pir);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A pending draw trigger cannot add a counter after Toothy leaves")
    void leavingBeforeDrawTriggerResolvesUsesOnlyExistingCounters() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PirImaginativeRascal(),
                new PirImaginativeRascal(), new PirImaginativeRascal()));
        Permanent toothy = harness.addToBattlefieldAndReturn(player1, new ToothyImaginaryFriend());
        toothy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, toothy));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void drawAndResolveTrigger(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }

}
