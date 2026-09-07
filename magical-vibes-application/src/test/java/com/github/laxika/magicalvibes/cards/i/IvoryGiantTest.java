package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvoryGiant.class, GrizzlyBears.class, SavannahLions.class})
class IvoryGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by tapping all nonwhite creatures")
    void entersByTappingAllNonwhiteCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLions = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingLions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        harness.setHand(player1, List.of(new IvoryGiant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
        assertThat(opposingBears.isTapped()).isTrue();
        assertThat(ownLions.isTapped()).isFalse();
        assertThat(opposingLions.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Suspend exiles Ivory Giant with five time counters")
    void suspendExilesWithFiveTimeCounters() {
        IvoryGiant card = new IvoryGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        IvoryGiant card = suspendCard();

        for (int i = 0; i < 4; i++) {
            removeOneTimeCounter();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ivory Giant")).isNotNull();
    }

    private IvoryGiant suspendCard() {
        IvoryGiant card = new IvoryGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void removeOneTimeCounter() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
