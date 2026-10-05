package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
import com.github.laxika.magicalvibes.cards.p.PerrieThePulverizer;
import com.github.laxika.magicalvibes.cards.r.RishkarPeemaRenegade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosDefenseContractor.class, GrizzlyBears.class, PerrieThePulverizer.class,
        DarksteelMutation.class, RishkarPeemaRenegade.class})
class KrosDefenseContractorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep shield counter taps, goads, and grants trample to the opposing creature")
    void upkeepAbilityTapsGoadsAndGrantsTrample() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary trample and goad expire at Kros's next turn")
    void temporaryEffectsExpireAtNextTurn() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }

    @Test
    void countersFromAnotherCardTriggerKrosWithoutTargetingAgain() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new PerrieThePulverizer());

        harness.castFromHand(player1, new PerrieThePulverizer(), "{1}{G}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void puttingCountersOnYourOwnCreatureDoesNotTriggerKros() {
        Permanent kros = addCreatureReady(player1, new KrosDefenseContractor());

        harness.castFromHand(player1, new PerrieThePulverizer(), "{1}{G}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kros.getId());
        resolveAllTriggers();

        assertThat(kros.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(kros.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, kros, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, kros)).isZero();
    }

    @Test
    void opponentsCounterPlacementDoesNotTriggerYourKros() {
        Permanent kros = addCreatureReady(player1, new KrosDefenseContractor());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new PerrieThePulverizer(), "{1}{G}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, kros.getId());
        resolveAllTriggers();

        assertThat(kros.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(kros.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, kros, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, kros)).isZero();
    }

    @Test
    void krosDoesNotTriggerAfterLosingItsAbilities() {
        Permanent kros = addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new PerrieThePulverizer());
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, kros.getId());
        resolveAllTriggers();
        assertThat(gqs.hasLostPrintedAbilities(gd, kros)).isTrue();

        harness.castFromHand(player1, new PerrieThePulverizer(), "{1}{G}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
    }

    @Test
    void nonShieldCountersTriggerSeparatelyForEachOpposingCreature() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent first = addCreatureReady(player2, new KrosDefenseContractor());
        Permanent second = addCreatureReady(player2, new PerrieThePulverizer());
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        for (Permanent target : List.of(first, second)) {
            assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(target.isTapped()).isTrue();
            assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
            assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
        }
    }

    @Test
    void upkeepDoesNothingWithoutAnOpposingCreature() {
        Permanent kros = addCreatureReady(player1, new KrosDefenseContractor());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(kros.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    void opponentsUpkeepDoesNotPutAShieldCounter() {
        addCreatureReady(player1, new KrosDefenseContractor());
        Permanent target = addCreatureReady(player2, new PerrieThePulverizer());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }
}
