package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RatOut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnawingCrescendo.class, RatOut.class, GrizzlyBears.class})
class GnawingCrescendoTest extends BaseCardTest {

    @Test
    void boostsYourCreaturesAndCreatesNonblockingRatsForYourNontokenDeaths() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGnawingCrescendo();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);

        opponentCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(findPermanents(player1, "Rat")).isEmpty();

        ownCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }

    @Test
    void doesNotTriggerWhenATokenCreatureDies() {
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        castGnawingCrescendo();

        rat.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void pumpExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void laterEnteringCreatureIsNotPumpedButItsDeathStillCreatesARat() {
        castGnawingCrescendo();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        laterCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, rats.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats.getFirst())).isEqualTo(1);
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }

    @Test
    void triggersForEachSimultaneousNontokenDeath() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();

        first.setMarkedDamage(2);
        second.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
    }

    @Test
    void triggersAgainForALaterDeathInTheSameTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();

        first.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Rat")).hasSize(1);

        second.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Rat")).hasSize(2);
    }

    @Test
    void multipleResolutionsStackBothPumpAndDeathTriggers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();
        castGnawingCrescendo();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
    }

    @Test
    void opponentsNontokenDeathDoesNotCreateARatAfterResolvingTriggers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGnawingCrescendo();

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(findPermanents(player2, "Rat")).isEmpty();
    }

    @Test
    void pumpsExistingTokensWithoutChangingTheirToughness() {
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent rat = findPermanents(player1, "Rat").getFirst();

        castGnawingCrescendo();

        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
    }

    @Test
    void deathDuringTheEndStepStillCreatesARat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();
        harness.forceStep(TurnStep.END_STEP);

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    void deathTriggerExpiresAfterTheTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    private void castGnawingCrescendo() {
        harness.setHand(player1, List.of(new GnawingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
