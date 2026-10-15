package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.cards.w.WipeAway;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepSeaKraken.class, AshcoatBear.class, WipeAway.class, TeferiMageOfZhalfir.class})
class DeepSeaKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Deep-Sea Kraken cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        Permanent attacker = addCreatureReady(player1, new DeepSeaKraken());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("After the last time counter, Deep-Sea Kraken may be cast for free with haste")
    void lastTimeCounterOffersFreeHastyCast() {
        DeepSeaKraken card = suspendCard();

        for (int i = 0; i < 9; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());

        Permanent kraken = findPermanent(player1, "Deep-Sea Kraken");
        assertThat(kraken.getCard()).isSameAs(card);
        assertThat(gqs.hasKeyword(gd, kraken, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Suspend exiles Deep-Sea Kraken with nine time counters")
    void suspendExilesWithNineTimeCounters() {
        DeepSeaKraken card = new DeepSeaKraken();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
    }

    @Test
    @DisplayName("An opponent casting a spell removes a time counter while Deep-Sea Kraken is suspended")
    void opponentSpellRemovesTimeCounter() {
        DeepSeaKraken card = suspendCard();
        Permanent target = addCreatureReady(player1, new AshcoatBear());

        harness.setHand(player2, List.of(new WipeAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 8);
    }

    @Test
    @DisplayName("Deep-Sea Kraken does not trigger when its owner casts a spell")
    void ownerSpellDoesNotRemoveTimeCounter() {
        DeepSeaKraken card = suspendCard();
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
    }

    @Test
    @DisplayName("Deep-Sea Kraken does not trigger on opponent spells while on the battlefield")
    void opponentSpellDoesNotTriggerOnBattlefield() {
        Permanent kraken = addCreatureReady(player1, new DeepSeaKraken());
        harness.setHand(player2, List.of(new WipeAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, kraken.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Removing the last time counter puts a separate suspend cast trigger on the stack")
    void lastCounterCreatesSeparateCastTrigger() {
        DeepSeaKraken card = suspendCard();
        gd.exiledCardTimeCounters.put(card.getId(), 1);

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Kraken exiled and no longer suspended")
    void decliningCastStopsOpponentSpellTriggers() {
        DeepSeaKraken card = suspendCard();
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());

        Permanent target = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player2, List.of(new WipeAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove a suspend counter")
    void opponentUpkeepDoesNotRemoveCounter() {
        DeepSeaKraken card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
    }

    @Test
    @DisplayName("An opponent spell removing the last counter allows Kraken to resolve before that spell")
    void lastCounterFromOpponentSpellCastsKrakenFirst() {
        DeepSeaKraken card = suspendCard();
        gd.exiledCardTimeCounters.put(card.getId(), 1);
        harness.setHand(player2, List.of(new AshcoatBear()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());

        Permanent kraken = findPermanent(player1, "Deep-Sea Kraken");
        assertThat(gqs.hasKeyword(gd, kraken, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player2, "Ashcoat Bear")).isZero();
    }

    @Test
    @DisplayName("Teferi granting flash allows suspending Kraken during an opponent's turn")
    void grantedFlashAllowsSuspendOnOpponentTurn() {
        addCreatureReady(player1, new TeferiMageOfZhalfir());
        DeepSeaKraken card = new DeepSeaKraken();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 9);
        assertThat(gd.stack).isEmpty();
    }

    private DeepSeaKraken suspendCard() {
        DeepSeaKraken card = new DeepSeaKraken();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
