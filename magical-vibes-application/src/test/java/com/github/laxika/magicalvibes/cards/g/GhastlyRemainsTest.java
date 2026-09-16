package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhastlyRemains.class, GempalmPolluter.class, GoblinTurncoat.class})
class GhastlyRemainsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each revealed Zombie card")
    void entersWithCountersForRevealedZombieCards() {
        GhastlyRemains card = new GhastlyRemains();
        GempalmPolluter firstZombie = new GempalmPolluter();
        GempalmPolluter secondZombie = new GempalmPolluter();
        harness.setHand(player1, List.of(card, firstZombie, secondZombie, new GoblinTurncoat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstZombie.getId(), secondZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId(), secondZombie.getId()));
        harness.passBothPriorities();

        Permanent remains = findPermanent(player1, "Ghastly Remains");
        assertThat(remains.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lets its controller choose how many Zombie cards to reveal for Amplify")
    void choosesHowManyZombieCardsToReveal() {
        GhastlyRemains card = new GhastlyRemains();
        GempalmPolluter firstZombie = new GempalmPolluter();
        GempalmPolluter secondZombie = new GempalmPolluter();
        harness.setHand(player1, List.of(card, firstZombie, secondZombie, new GoblinTurncoat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstZombie.getId(), secondZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ghastly Remains")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May pay {B}{B}{B} during your upkeep to return it from your graveyard to your hand")
    void paysToReturnFromGraveyardToHand() {
        GhastlyRemains card = new GhastlyRemains();
        harness.setGraveyard(player1, List.of(card));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(card.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves it in the graveyard")
    void decliningPaymentLeavesItInGraveyard() {
        GhastlyRemains card = new GhastlyRemains();
        harness.setGraveyard(player1, List.of(card));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(card.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(card.getId()));
    }
}
