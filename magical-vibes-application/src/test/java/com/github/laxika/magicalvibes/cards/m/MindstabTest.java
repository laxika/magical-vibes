package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindstab.class, AshcoatBear.class, PithingNeedle.class})
class MindstabTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards three cards")
    void targetPlayerDiscardsThreeCards() {
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear(), new AshcoatBear()));
        harness.setHand(player1, List.of(new Mindstab()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Suspend counters are removed only during Mindstab's owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        Mindstab card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that makes the target player discard three cards")
    void lastCounterOffersFreeCastAndDiscardsThreeCards() {
        Mindstab card = suspendCard();
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear(), new AshcoatBear()));

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Mindstab");
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Mindstab in exile")
    void decliningSuspendCastLeavesCardInExile() {
        Mindstab card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotInGraveyard(player1, "Mindstab");
    }

    @Test
    @DisplayName("Suspend exiles Mindstab with four time counters")
    void suspendExilesWithFourTimeCounters() {
        Mindstab card = new Mindstab();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    private Mindstab suspendCard() {
        Mindstab card = new Mindstab();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    @Test
    @DisplayName("Pithing Needle cannot prevent the suspend special action")
    void suspendIsNotAnActivatedAbility() {
        harness.addToBattlefieldAndReturn(player2, new PithingNeedle()).setChosenName("Mindstab");
        Mindstab card = new Mindstab();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player with fewer than three cards discards their whole hand")
    void targetWithTwoCardsDiscardsBoth() {
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear()));
        harness.setHand(player1, List.of(new Mindstab()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Mindstab");
    }

    @Test
    @DisplayName("An empty-handed target discards nothing")
    void emptyHandDoesNotRequireDiscardChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Mindstab()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Mindstab");
    }

    @Test
    @DisplayName("Mindstab can target its caster")
    void casterCanDiscardTheirOwnCards() {
        harness.setHand(player1, List.of(new Mindstab(), new AshcoatBear(),
                new AshcoatBear(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.discardCausedByOpponent).isFalse();
    }

    @Test
    @DisplayName("The targeted player chooses exactly three cards from a larger hand")
    void targetChoosesWhichThreeCardsToDiscard() {
        AshcoatBear retained = new AshcoatBear();
        AshcoatBear first = new AshcoatBear();
        AshcoatBear second = new AshcoatBear();
        AshcoatBear third = new AshcoatBear();
        harness.setHand(player2, List.of(retained, first, second, third));
        harness.setHand(player1, List.of(new Mindstab()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
