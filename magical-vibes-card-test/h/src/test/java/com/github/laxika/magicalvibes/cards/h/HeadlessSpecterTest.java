package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeadlessSpecter.class})
class HeadlessSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("With an empty hand, combat damage makes the damaged player discard at random")
    void emptyHandTriggersRandomDiscard() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HeadlessSpecter()));

        Permanent specter = addCreatureReady(player1, new HeadlessSpecter());
        specter.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Headless Specter");
    }

    @Test
    @DisplayName("With a card in hand, combat damage does not trigger the hellbent ability")
    void cardsInHandPreventTrigger() {
        harness.setHand(player1, List.of(new HeadlessSpecter()));
        harness.setHand(player2, List.of(new HeadlessSpecter()));

        Permanent specter = addCreatureReady(player1, new HeadlessSpecter());
        specter.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void gainingACardBeforeResolutionPreventsDiscard() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HeadlessSpecter()));
        addCreatureReady(player1, new HeadlessSpecter()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new HeadlessSpecter()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyingHandAfterDamageDoesNotCreateATrigger() {
        harness.setHand(player1, List.of(new HeadlessSpecter()));
        harness.setHand(player2, List.of(new HeadlessSpecter()));
        addCreatureReady(player1, new HeadlessSpecter()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void randomDiscardRemovesExactlyOneCardWithoutAChoice() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HeadlessSpecter(), new HeadlessSpecter(), new HeadlessSpecter()));
        addCreatureReady(player1, new HeadlessSpecter()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void damagedPlayerWithEmptyHandDiscardsNothing() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new HeadlessSpecter()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void blockedSpecterDoesNotMakeDefendingPlayerDiscard() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HeadlessSpecter()));
        addCreatureReady(player1, new HeadlessSpecter()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HeadlessSpecter());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
