package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvoryGiant.class, AshcoatBear.class, BenalishCavalry.class, Forest.class})
class IvoryGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by tapping all nonwhite creatures")
    void entersByTappingAllNonwhiteCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent ownLions = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent opposingLions = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new IvoryGiant(), "{5}{W}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
        assertThat(opposingBears.isTapped()).isTrue();
        assertThat(ownLions.isTapped()).isFalse();
        assertThat(opposingLions.isTapped()).isFalse();
        assertThat(ownForest.isTapped()).isFalse();
        assertThat(opposingForest.isTapped()).isFalse();
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
    @DisplayName("Suspend counters are removed only during Ivory Giant's owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        IvoryGiant card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
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

        Permanent permanent = findPermanent(player1, "Ivory Giant");
        assertThat(permanent).isNotNull();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Ivory Giant in exile")
    void decliningCastLeavesCardInExile() {
        IvoryGiant card = suspendCard();

        for (int i = 0; i < 4; i++) {
            removeOneTimeCounter();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(card);
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
