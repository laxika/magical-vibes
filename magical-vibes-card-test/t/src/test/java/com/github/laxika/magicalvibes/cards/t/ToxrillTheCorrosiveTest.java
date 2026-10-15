package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToxrillTheCorrosive.class, AirElemental.class, Forest.class, Frogify.class, GrizzlyBears.class})
class ToxrillTheCorrosiveTest extends BaseCardTest {

    @Test
    @DisplayName("The end-step trigger adds slime counters only to opponents' creatures")
    void endStepAddsSlimeCountersToOpponentsCreatures() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        opponentCreature.setCounterCount(CounterType.SLIME, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.SLIME)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.SLIME)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A slimed opponent creature's death creates exactly one Slug token")
    void slimedOpponentCreatureDeathCreatesOneSlug() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        opponentCreature.setCounterCount(CounterType.SLIME, 2);
        opponentCreature.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Slug")).hasSize(1);
    }

    @Test
    @DisplayName("A creature without a slime counter does not create a Slug token when it dies")
    void creatureWithoutSlimeCounterDoesNotCreateSlug() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Slug")).isEmpty();
    }

    @Test
    @DisplayName("The activated ability can sacrifice Toxrill itself and draw a card")
    void sacrificeSlugDrawsCard() {
        Permanent source = addCreatureReady(player1, new ToxrillTheCorrosive());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("The trigger also works during an opponent's end step and ignores noncreatures")
    void triggersDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent opponent = addCreatureReady(player2, new AirElemental());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(opponent.getCounterCount(CounterType.SLIME)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.SLIME)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(3);
    }

    @Test
    @DisplayName("Slime counters on an own creature neither weaken it nor create a Slug when it dies")
    void ownSlimedCreatureDoesNotCreateSlug() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        own.setCounterCount(CounterType.SLIME, 2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        own.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(own);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Slug")).isEmpty();
    }

    @Test
    @DisplayName("Each opponent creature reduced to zero toughness creates one Slug")
    void zeroToughnessDeathsCreateOneSlugPerCreature() {
        harness.addToBattlefield(player1, new ToxrillTheCorrosive());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.SLIME, 2);
        second.setCounterCount(CounterType.SLIME, 3);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(findPermanents(player1, "Slug")).hasSize(2);
    }

    @Test
    @DisplayName("Toxrill sees a slimed creature die simultaneously with itself")
    void simultaneousDeathStillCreatesSlug() {
        Permanent source = addCreatureReady(player1, new ToxrillTheCorrosive());
        Permanent opponent = addCreatureReady(player2, new ToxrillTheCorrosive());
        opponent.setCounterCount(CounterType.SLIME, 1);
        opponent.setMarkedDamage(6);
        source.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponent);
        assertThat(findPermanents(player1, "Slug")).hasSize(1);
        assertThat(findPermanents(player2, "Slug")).isEmpty();
    }

    @Test
    @DisplayName("A pending end-step trigger adds counters after Toxrill leaves without weakening creatures")
    void endStepTriggerResolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new ToxrillTheCorrosive());
        Permanent opponent = addCreatureReady(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        source.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(opponent.getCounterCount(CounterType.SLIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(4);
    }

    @Test
    @DisplayName("A generated Slug pays the sacrifice cost without sacrificing Toxrill")
    void generatedSlugPaysSacrificeCost() {
        Permanent source = addCreatureReady(player1, new ToxrillTheCorrosive());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.SLIME, 2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        Permanent slug = findPermanent(player1, "Slug");
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, slug.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(slug);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Toxrill cannot trigger for a slimed creature's death after Frogify removes its abilities")
    void continuousAbilityRemovalPreventsDeathTrigger() {
        Permanent source = addCreatureReady(player1, new ToxrillTheCorrosive());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Frogify()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0, source.getId());
        harness.passBothPriorities();
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.SLIME, 1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
        opponent.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Slug")).isEmpty();
    }

}
