package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DescentIntoMadnessTest extends BaseCardTest {

    @Test
    @DisplayName("First upkeep adds a despair counter and each player exiles one object")
    void firstUpkeepExilesOneEach() {
        emptyHands();
        Permanent descent = addDescent(player1);
        Permanent bears = addCreature(player1, new GrizzlyBears());
        Permanent lions = addCreature(player2, new SavannahLions());

        triggerUpkeep(player1);

        assertThat(descent.getCounterCount(CounterType.DESPAIR)).isEqualTo(1);

        // Player 1 controls Descent + Grizzly Bears, so they must choose; player 2 only has the
        // Lions, which is auto-exiled with no prompt.
        answerChoice(player1, bears.getCard().getId());

        assertThat(battlefield(player1)).doesNotContain(bears);
        assertThat(battlefield(player2)).doesNotContain(lions);
        assertThat(exiledCardIds()).contains(bears.getCard().getId(), lions.getCard().getId());
        assertThat(battlefield(player1)).contains(descent);
    }

    @Test
    @DisplayName("Exile count scales with despair counters already on the enchantment")
    void exileCountScalesWithCounters() {
        emptyHands();
        Permanent descent = addDescent(player1);
        descent.setCounterCount(CounterType.DESPAIR, 1);
        Permanent bears = addCreature(player1, new GrizzlyBears());
        Permanent lions = addCreature(player1, new SavannahLions());

        triggerUpkeep(player1);

        // Counter goes to 2, so player 1 exiles two of their three permanents.
        assertThat(descent.getCounterCount(CounterType.DESPAIR)).isEqualTo(2);
        answerChoice(player1, bears.getCard().getId(), lions.getCard().getId());

        assertThat(battlefield(player1)).containsExactly(descent);
        assertThat(exiledCardIds()).contains(bears.getCard().getId(), lions.getCard().getId());
    }

    @Test
    @DisplayName("A player may exile cards from their hand instead of permanents")
    void handCardsAreValidChoices() {
        emptyHands();
        addDescent(player1);
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));

        triggerUpkeep(player1);

        // Descent itself plus the hand card are the two choosable objects; keeping Descent means
        // exiling the hand card.
        answerChoice(player1, handCard.getId());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(handCard);
        assertThat(exiledCardIds()).contains(handCard.getId());
    }

    @Test
    @DisplayName("Descent into Madness can eat itself when it is the only object")
    void exilesItselfWhenSoleObject() {
        emptyHands();
        Permanent descent = addDescent(player1);

        triggerUpkeep(player1);

        // Only one object: no prompt, it is exiled automatically.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(battlefield(player1)).doesNotContain(descent);
        assertThat(exiledCardIds()).contains(descent.getCard().getId());
    }

    @Test
    @DisplayName("A player with no permanents and an empty hand exiles nothing")
    void emptyPlayerExilesNothing() {
        emptyHands();
        addDescent(player1);
        Permanent bears = addCreature(player1, new GrizzlyBears());

        triggerUpkeep(player1);
        answerChoice(player1, bears.getCard().getId());

        assertThat(battlefield(player2)).isEmpty();
        assertThat(exiledCardIds()).containsExactly(bears.getCard().getId());
    }

    /** Hand cards are legal picks, so the default starting hands must be cleared to isolate a case. */
    private void emptyHands() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private Permanent addDescent(Player owner) {
        return addCreature(owner, new DescentIntoMadness());
    }

    private Permanent addCreature(Player owner, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private List<Permanent> battlefield(Player player) {
        return gd.playerBattlefields.get(player.getId());
    }

    private List<UUID> exiledCardIds() {
        return gd.exiledCards.stream().map(e -> e.card().getId()).toList();
    }

    private void triggerUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP queues the trigger
        harness.passBothPriorities(); // resolve the trigger
    }

    private void answerChoice(Player player, UUID... cardIds) {
        PendingInteraction.ExilePermanentsOrHandCardsChoice pending = gd.interaction
                .activeInteraction(PendingInteraction.ExilePermanentsOrHandCardsChoice.class);
        assertThat(pending).isNotNull();
        assertThat(pending.playerId()).isEqualTo(player.getId());
        harness.handleMultipleCardsChosen(player, List.of(cardIds));
    }
}
