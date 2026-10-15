package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BagEndBanquet;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TastefulOffering.class, BagEndBanquet.class, GrizzlyBears.class, BarterInBlood.class, Swamp.class})
class TastefulOfferingTest extends BaseCardTest {

    @Test
    void createsFoodAndSeeksForTheFirstTwoSacrificesOnly() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new BagEndBanquet(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new TastefulOffering(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(4);

        sacrificeFoodAndResolve();
        sacrificeFoodAndResolve();
        sacrificeFoodAndResolve();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void seeksOnlyNonlandsAndFoodGainsThreeLife() {
        Swamp firstLand = new Swamp();
        TastefulOffering nonland = new TastefulOffering();
        Swamp secondLand = new Swamp();
        harness.setLibrary(player1, List.of(firstLand, nonland, secondLand));
        harness.setLife(player1, 10);

        castOffering();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        sacrificeFoodAndResolve();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstLand, secondLand);
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 13);
    }

    @Test
    void thirdSeparateSacrificeDoesNotSeekEvenWhenNonlandsRemain() {
        harness.castFromHand(player1, new BagEndBanquet(), "{6}");
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(
                new TastefulOffering(), new TastefulOffering(), new TastefulOffering()));
        castOffering();

        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void simultaneousSacrificesSeekOnceAndLeaveOneBoonUse() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(
                new TastefulOffering(), new TastefulOffering(), new TastefulOffering()));
        castOffering();

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsSacrificeDoesNotConsumeTheBoon() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new TastefulOffering(), new TastefulOffering()));
        castOffering();

        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        resolveAllTriggers();
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void sacrificeWithNoMatchingCardStillConsumesOneUse() {
        harness.castFromHand(player1, new BagEndBanquet(), "{6}");
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new Swamp()));
        castOffering();

        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.setLibrary(player1, List.of(new TastefulOffering(), new TastefulOffering()));
        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void multipleBoonsEachTriggerForTheSameSacrifice() {
        harness.setLibrary(player1, List.of(
                new TastefulOffering(), new TastefulOffering(),
                new TastefulOffering(), new TastefulOffering()));
        castOffering();
        castOffering();

        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        sacrificeFoodAndResolve();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castOffering() {
        harness.castFromHand(player1, new TastefulOffering(), "{1}{B}");
        resolveAllTriggers();
    }

    private void sacrificeFoodAndResolve() {
        Permanent food = findPermanents(player1, "Food").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(food), 0, null);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
