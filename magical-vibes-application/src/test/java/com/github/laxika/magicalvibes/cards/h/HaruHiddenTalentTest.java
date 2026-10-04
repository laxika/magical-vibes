package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EarthenAlly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaruHiddenTalent.class, EarthenAlly.class, Forest.class, GrizzlyBears.class})
class HaruHiddenTalentTest extends BaseCardTest {

    @Test
    void anotherAllyEnteringEarthbendsLandYouControl() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonAllyEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void triggerCannotTargetOpponentLand() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        assertThat(choice.validIds()).doesNotContain(opponentLand.getId());
    }

    @Test
    void haruEnteringDoesNotTriggerItself() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new HaruHiddenTalent());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void opponentAllyEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.enterBattlefieldAndReturn(player2, new EarthenAlly());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void anotherAllyAddsACounterToAnAlreadyEarthbendedLand() {
        Permanent land = earthbendForest();

        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }

    @Test
    void dyingEarthbendedLandReturnsTappedWithoutAnimationOrCounters() {
        Permanent land = earthbendForest();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, land));
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedForest(land);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void exiledEarthbendedLandReturnsTappedWithoutAnimationOrCounters() {
        Permanent land = earthbendForest();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        assertReturnedForest(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    void bouncedEarthbendedLandDoesNotReturn() {
        Permanent land = earthbendForest();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, land));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void earthbendedLandStillReturnsAfterHaruLeaves() {
        Permanent land = earthbendForest();
        Permanent haru = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Haru, Hidden Talent"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, haru));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertReturnedForest(land);
        harness.assertInGraveyard(player1, "Haru, Hidden Talent");
    }

    @Test
    void noControlledLandLeavesNoTargetChoiceOrAbilityOnStack() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        harness.addToBattlefield(player2, new Forest());

        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landLeavingBeforeResolutionIsNotEarthbendedOrReturned() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(land.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent earthbendForest() {
        harness.addToBattlefield(player1, new HaruHiddenTalent());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new EarthenAlly());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        return land;
    }

    private void assertReturnedForest(Permanent original) {
        harness.assertOnBattlefield(player1, "Forest");
        Permanent returned = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Forest"));
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCard().getId()).isEqualTo(original.getCard().getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
