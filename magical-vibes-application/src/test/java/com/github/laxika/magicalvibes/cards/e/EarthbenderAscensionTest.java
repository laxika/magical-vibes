package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthbenderAscension.class, Forest.class, GrizzlyBears.class})
class EarthbenderAscensionTest extends BaseCardTest {

    @Test
    void entersByEarthbendingExistingLandThenSearchingTappedBasicLand() {
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.castFromHand(player1, new EarthbenderAscension(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice earthbendChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(earthbendChoice.validIds()).containsExactly(existingLand.getId());
        harness.handlePermanentChosen(player1, existingLand.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gqs.isCreature(gd, existingLand)).isTrue();
        assertThat(existingLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND)
                        && permanent.isTapped()
                        && permanent != existingLand);
    }

    @Test
    void landfallAtFourQuestCountersTargetsOwnCreatureForCounterAndTrample() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        for (int i = 0; i < 4; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.passBothPriorities();
        }

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void fewerThanFourQuestCountersDoNotBoostCreature() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (int i = 1; i <= 3; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.passBothPriorities();

            assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(i);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Test
    void opponentsLandDoesNotAddQuestCounter() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void subsequentLandfallsKeepBoostingAndOnlyTrampleExpires() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ascension.setCounterCount(CounterType.QUEST, 4);

        for (int i = 1; i <= 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, creature.getId());
            harness.passBothPriorities();

            assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4 + i);
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(i);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        }

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void questCounterIsPlacedEvenWhenThereAreNoCreaturesToTarget() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        ascension.setCounterCount(CounterType.QUEST, 3);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reflexiveAbilityRechecksQuestCounterThresholdAtResolution() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ascension.setCounterCount(CounterType.QUEST, 3);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        ascension.setCounterCount(CounterType.QUEST, 3);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void reflexiveAbilityUsesLastKnownCountersAfterAscensionLeaves() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new EarthbenderAscension());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ascension.setCounterCount(CounterType.QUEST, 3);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ascension);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void losingEarthbendTargetPreventsTheEntireEntryAbilityFromResolving() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new EarthbenderAscension(), "{2}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        harness.getPermanentRemovalService().removePermanentToHand(gd, land);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> gqs.isLand(gd, permanent));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void earthbendedLandRemainsAnimatedAndReturnsTappedAfterDyingOrExile(boolean exile) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new EarthbenderAscension(), "{2}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();

        if (exile) {
            harness.getPermanentRemovalService().removePermanentToExile(gd, land);
        } else {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returnedLand.getId()).isNotEqualTo(land.getId());
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
        assertThat(returnedLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Earthbender Ascension").getCounterCount(CounterType.QUEST))
                .isEqualTo(2);
    }
}
