package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DayOfBlackSun;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarKyoshiEarthbender.class, Forest.class, DayOfBlackSun.class})
class AvatarKyoshiEarthbenderTest extends BaseCardTest {

    @Test
    void earthbendsAndUntapsTargetLandAtBeginningOfOwnCombat() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(8);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void hasHexproofOnlyDuringItsControllerTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new AvatarKyoshiEarthbender());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, avatar, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, avatar, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void onlyOffersLandsControlledByTheAbilityController() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownLand.getId());
    }

    @Test
    void earthbendingTheSameLandAgainAddsEightMoreCounters() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(land);
        land.tap();

        earthbend(land);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(16);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(16);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void animationAndHastePersistOnTheNextTurn() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(land);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void earthbendedLandReturnsTappedAfterDying() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(land);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, land));
        resolveAllTriggers();

        assertReturnedAsOrdinaryTappedLand();
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(land);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        resolveAllTriggers();

        assertReturnedAsOrdinaryTappedLand();
    }

    @Test
    void losingAbilitiesDoesNotRemoveTheEarthbendDelayedReturn() {
        harness.addToBattlefield(player1, new AvatarKyoshiEarthbender());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(land);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new DayOfBlackSun()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertReturnedAsOrdinaryTappedLand();
    }

    private void earthbend(Permanent land) {
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
    }

    private void assertReturnedAsOrdinaryTappedLand() {
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
