package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.c.CoercedToKill;
import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharpEyedRookie.class, CentaurCourser.class, GiantTortoise.class, GrizzlyBears.class,
        SampleCollector.class, Murder.class, Solemnity.class, CoercedToKill.class})
class SharpEyedRookieTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself and investigates when an entering creature has greater power")
    void triggersForGreaterPower() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());

        harness.castFromHand(player1, new CentaurCourser(), "{2}{G}");
        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Triggers when an entering creature has greater toughness")
    void triggersForGreaterToughness() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());

        harness.castFromHand(player1, new GiantTortoise(), "{1}{U}");
        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when an entering creature has neither greater power nor toughness")
    void doesNotTriggerWhenNeitherCharacteristicIsGreater() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatesDuringTheSameResolutionAsTheCounter() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void investigatesEvenWhenCountersCannotBePlaced() {
        harness.addToBattlefield(player1, new Solemnity());
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());

        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatesUsingLastKnownStatsWhenRookieIsDestroyedInResponse() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, rookie.getId());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sharp-Eyed Rookie");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void rechecksStatsForEachPendingTrigger() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());

        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player2, new SampleCollector());

        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        Permanent rookie = harness.enterBattlefieldAndReturn(player1, new SharpEyedRookie());

        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void usesLastKnownStatsWhenTheEnteringCreatureIsDestroyed() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new SampleCollector());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, entering.getId());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sample Collector");
        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void originalAbilityControllerInvestigatesAfterRookieChangesController() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());
        harness.setHand(player2, List.of(new CoercedToKill()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0, rookie.getId());

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Sharp-Eyed Rookie");
        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void attackingDoesNotTapRookie() {
        Permanent rookie = addCreatureReady(player1, new SharpEyedRookie());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(rookie.isTapped()).isFalse();
    }

    @Test
    void createdClueCanBeSacrificedForTwoManaToDrawACard() {
        harness.addToBattlefield(player1, new SharpEyedRookie());
        harness.enterBattlefieldAndReturn(player1, new SampleCollector());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new SharpEyedRookie()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));

        harness.activateAbility(player1, clueIndex, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInHand(player1, "Sharp-Eyed Rookie");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
