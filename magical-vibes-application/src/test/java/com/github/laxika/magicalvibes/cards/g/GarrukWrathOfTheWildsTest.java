package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrukWrathOfTheWilds.class, GrizzlyBears.class})
class GarrukWrathOfTheWildsTest extends BaseCardTest {

    @Test
    void plusOneAppliesBothPerpetualChangesToTheChosenCreatureCard() {
        Permanent garruk = addReadyGarruk(4);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(bear));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteredBear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, enteredBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enteredBear)).isEqualTo(3);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusOneDraftsAChoiceOntoTheBattlefield() {
        Permanent garruk = addReadyGarruk(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(drafted.getId()));
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void minusSixBoostsOwnCreaturesAndGrantsTrampleUntilEndOfTurn() {
        Permanent garruk = addReadyGarruk(6);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyGarruk(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GarrukWrathOfTheWilds());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    void plusOneWithNoCreatureInHandStillAddsLoyalty() {
        Permanent garruk = addReadyGarruk(4);
        harness.setHand(player1, List.of(new GarrukWrathOfTheWilds()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @CardUsed(Unsummon.class)
    void plusOneChangesPersistAfterReturningToHandAndRecasting() {
        addReadyGarruk(4);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Unsummon()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bear = findPermanent(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.assertInHand(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recastBear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, recastBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recastBear)).isEqualTo(3);
    }

    @Test
    void minusOneOffersThreeDistinctSpellbookCards() {
        addReadyGarruk(4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards().stream().map(Card::getName).toList()).doesNotHaveDuplicates();
    }

    @Test
    void minusSixDoesNotAffectCreaturesEnteringAfterResolution() {
        addReadyGarruk(7);
        Permanent existingBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent laterBear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingBear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, existingBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void minusSixBonusesExpireAtCleanup() {
        addReadyGarruk(7);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed(SarkhanTheMasterless.class)
    void minusSixAlsoGrantsTrampleToGarrukWhenHeIsACreature() {
        Permanent garruk = addReadyGarruk(7);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, garruk)).isTrue();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, garruk)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, garruk)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, garruk, Keyword.TRAMPLE)).isTrue();
    }
}
