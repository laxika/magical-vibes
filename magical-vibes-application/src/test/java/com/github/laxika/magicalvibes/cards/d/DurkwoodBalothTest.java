package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DurkwoodBaloth.class})
class DurkwoodBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Durkwood Baloth with five time counters")
    void suspendExilesWithFiveTimeCounters() {
        DurkwoodBaloth card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend counters are removed only during Durkwood Baloth's owner's upkeep")
    void suspendCountersAreRemovedOnlyDuringOwnersUpkeep() {
        DurkwoodBaloth card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        DurkwoodBaloth card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Durkwood Baloth");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Durkwood Baloth in exile")
    void decliningSuspendCastLeavesCardInExile() {
        DurkwoodBaloth card = suspendCard();

        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotInGraveyard(player1, "Durkwood Baloth");
    }

    private DurkwoodBaloth suspendCard() {
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    @Test
    @DisplayName("Suspend requires its green mana payment")
    void suspendRequiresMana() {
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }

    @Test
    @DisplayName("Durkwood Baloth cannot normally be suspended during upkeep")
    void cannotSuspendDuringUpkeep() {
        DurkwoodBaloth card = new DurkwoodBaloth();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The upkeep counter is removed when the trigger resolves")
    void upkeepCounterRemovalUsesStack() {
        DurkwoodBaloth card = suspendCard();

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("Casting Durkwood Baloth normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.setHand(player1, List.of(new DurkwoodBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Durkwood Baloth");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
        assertThat(gd.exiledCardTimeCounters).isEmpty();
    }
}
