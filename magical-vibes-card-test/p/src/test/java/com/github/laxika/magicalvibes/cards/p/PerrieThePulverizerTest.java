package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerrieThePulverizer.class, Forest.class, GrizzlyBears.class})
class PerrieThePulverizerTest extends BaseCardTest {

    @Test
    void entersAndPutsShieldCounterOnTargetCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new PerrieThePulverizer(), "{1}{G}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void attackingCreatureGetsTrampleAndBoostEqualToDistinctControlledCounterKinds() {
        Permanent perrie = addCreatureReady(player1, new PerrieThePulverizer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        forest.setCounterCount(CounterType.CHARGE, 1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(target.getId(), perrie.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void entersAndCanPutShieldCounterOnOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player1, new PerrieThePulverizer(), "{1}{G}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void canTargetItselfAndGrantsTrampleWithNoCountersUntilEndOfTurn() {
        Permanent perrie = addCreatureReady(player1, new PerrieThePulverizer());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, perrie.getId());
        resolveAllTriggers();

        assertThat(perrie.getPowerModifier()).isZero();
        assertThat(perrie.getToughnessModifier()).isZero();
        assertThat(perrie.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(perrie.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    void countsEachKindOnceAndIgnoresOpponentCounters() {
        Permanent perrie = addCreatureReady(player1, new PerrieThePulverizer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        perrie.setCounterCount(CounterType.SHIELD, 2);
        target.setCounterCount(CounterType.SHIELD, 3);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        opponent.setCounterCount(CounterType.CHARGE, 1);

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(perrie.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void countsCountersAtResolutionAndStillResolvesAfterPerrieLeaves() {
        Permanent perrie = addCreatureReady(player1, new PerrieThePulverizer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        perrie.setCounterCount(CounterType.SHIELD, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(perrie);
        forest.setCounterCount(CounterType.CHARGE, 2);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }
}
