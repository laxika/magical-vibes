package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmbroseDeanOfShadow;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MascotInterception;
import com.github.laxika.magicalvibes.cards.u.UnwillingIngredient;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShaileDeanOfRadiance.class, EmbroseDeanOfShadow.class, Forest.class,
        GrizzlyBears.class, LlanowarElves.class, WrathOfGod.class,
        EagerFirstYear.class, MascotInterception.class, UnwillingIngredient.class})
class ShaileDeanOfRadianceTest extends BaseCardTest {

    @Test
    void shailePutsCountersOnControlledCreaturesThatEnteredThisTurn() {
        harness.setHand(player1, List.of(new ShaileDeanOfRadiance(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent shaile = gd.playerBattlefields.get(player1.getId()).getFirst();
        shaile.setSummoningSick(false);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent recentCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void embroseCanBeCastAsTheBackFaceAndDamagesTheCreatureItCounters() {
        harness.setHand(player1, List.of(new ShaileDeanOfRadiance()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent embrose = findPermanent(player1, "Embrose, Dean of Shadow");
        embrose.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void embroseCannotTargetItself() {
        harness.setHand(player1, List.of(new ShaileDeanOfRadiance()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent embrose = findPermanent(player1, "Embrose, Dean of Shadow");
        embrose.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, embrose.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void embroseDrawsWhenAControlledCreatureWithACounterDies() {
        addReadyEmbrose();
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void shaileIgnoresCreaturesThatDidNotEnterThisTurn() {
        Permanent shaile = addReadyShaile();
        Permanent oldCreature = addCreatureReady(player1, new EagerFirstYear());
        Permanent recentCreature = harness.enterBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shaile.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(oldCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(recentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void shaileCountersCreaturesThatEnteredUnderYourControlEvenAfterAnOpponentTakesThem() {
        addReadyShaile();
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MascotInterception()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player2, 0, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void shaileDoesNotCounterCreaturesThatEnteredUnderAnOpponentsControlAndWereStolen() {
        addReadyShaile();
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new EagerFirstYear());
        harness.setHand(player1, List.of(new MascotInterception()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void embroseAddsTheCounterBeforeDealingDamage() {
        addReadyEmbrose();
        Permanent target = addCreatureReady(player1, new EagerFirstYear());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eager First-Year");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void embroseDrawsWhenItsAbilityKillsAControlledCreature() {
        addReadyEmbrose();
        Permanent target = addCreatureReady(player1, new UnwillingIngredient());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Unwilling Ingredient");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void embroseDoesNotDrawWhenItsAbilityKillsAnOpponentsCreature() {
        addReadyEmbrose();
        Permanent target = addCreatureReady(player2, new UnwillingIngredient());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Unwilling Ingredient");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void embroseDrawsForItsOwnDeathIfItHasAPlusOneCounter() {
        Permanent embrose = addReadyEmbrose();
        embrose.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Embrose, Dean of Shadow");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void embroseDoesNotDrawForCreaturesWithoutPlusOneCounters() {
        addReadyEmbrose();
        addCreatureReady(player1, new EagerFirstYear());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void embroseDrawsOnceForEachCounteredCreatureDyingSimultaneously() {
        addReadyEmbrose();
        Permanent first = addCreatureReady(player1, new EagerFirstYear());
        Permanent second = addCreatureReady(player1, new UnwillingIngredient());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private Permanent addReadyShaile() {
        Permanent shaile = addCreatureReady(player1, new ShaileDeanOfRadiance());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return shaile;
    }

    private Permanent addReadyEmbrose() {
        ShaileDeanOfRadiance card = new ShaileDeanOfRadiance();
        Permanent embrose = harness.addToBattlefieldAndReturn(player1, card);
        embrose.setCard(card.getBackFaceCard());
        embrose.setTransformed(true);
        embrose.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return embrose;
    }
}
