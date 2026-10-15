package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenOfEnduringHope;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PippinWardenOfIsengard;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreebeardGraciousHost.class, AvenOfEnduringHope.class, RedwoodTreefolk.class,
        GrizzlyBears.class, PippinWardenOfIsengard.class})
class TreebeardGraciousHostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Food tokens")
    void entersWithTwoFoodTokens() {
        harness.castFromHand(player1, new TreebeardGraciousHost(), "{2}{G}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(2);
        assertThat(findPermanents(player1, "Food")).allSatisfy(food -> {
            assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        });
    }

    @Test
    @DisplayName("Life gain puts that many counters on a target Halfling or Treefolk")
    void lifeGainPutsCountersEqualToLifeGained() {
        harness.addToBattlefield(player1, new TreebeardGraciousHost());
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new RedwoodTreefolk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new AvenOfEnduringHope(), "{4}{W}");
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(treefolk.getId());
        assertThat(choice.validIds()).doesNotContain(
                harness.getPermanentId(player1, "Grizzly Bears"));

        harness.handlePermanentChosen(player1, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Food can be sacrificed for life and counters on Treebeard itself")
    void foodsGainLifeAndTriggerSeparately() {
        harness.castFromHand(player1, new TreebeardGraciousHost(), "{2}{G}{W}");
        resolveAllTriggers();
        Permanent treebeard = findPermanent(player1, "Treebeard, Gracious Host");
        harness.setLife(player1, 20);

        for (int i = 1; i <= 2; i++) {
            Permanent food = findPermanent(player1, "Food");
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 1, null, null);

            assertThat(findPermanents(player1, "Food")).doesNotContain(food);
            harness.assertLife(player1, 20 + (i - 1) * 3);
            harness.passBothPriorities();
            harness.assertLife(player1, 20 + i * 3);
            harness.handlePermanentChosen(player1, treebeard.getId());
            harness.passBothPriorities();

            assertThat(treebeard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i * 3);
        }
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    @DisplayName("Life gain can put counters on an opponent's Halfling")
    void canTargetOpponentsHalfling() {
        harness.castFromHand(player1, new TreebeardGraciousHost(), "{2}{G}{W}");
        resolveAllTriggers();
        Permanent halfling = harness.addToBattlefieldAndReturn(player2, new PippinWardenOfIsengard());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(halfling.getId());
        harness.handlePermanentChosen(player1, halfling.getId());
        harness.passBothPriorities();

        assertThat(halfling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger Treebeard")
    void opponentsLifeGainDoesNotTrigger() {
        Permanent treebeard = harness.addToBattlefieldAndReturn(player1, new TreebeardGraciousHost());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AvenOfEnduringHope(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player2, 23);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(treebeard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
