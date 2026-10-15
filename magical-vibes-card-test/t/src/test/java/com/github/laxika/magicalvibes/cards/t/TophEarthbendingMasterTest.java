package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TophEarthbendingMaster.class, Forest.class, GrizzlyBears.class})
class TophEarthbendingMasterTest extends BaseCardTest {

    @Test
    void opposingLandDoesNotGiveExperience() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void multipleLandEntriesEachGiveExperience() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
    }

    @Test
    void multipleAttackersEarthbendOnlyOnce() {
        addCreatureReady(player1, new TophEarthbendingMaster());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void zeroExperienceLandDiesAndReturnsTappedThenTriggersLandfall() {
        addCreatureReady(player1, new TophEarthbendingMaster());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Forest");

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");

        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void exiledEarthbendedLandReturnsTappedWithoutAnimationOrCounters() {
        addCreatureReady(player1, new TophEarthbendingMaster());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));

        assertThat(gd.findExiledCard(land.getCard().getId())).isNotNull();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();

        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 3);
    }

    @Test
    void earthbendUsesExperienceTotalAtResolution() {
        addCreatureReady(player1, new TophEarthbendingMaster());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, land.getId());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void landfallGivesAnExperienceCounter() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void attackingEarthbendsAChosenLandByTheNumberOfExperienceCounters() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackTriggerCannotTargetAnOpponentsLand() {
        harness.addToBattlefield(player1, new TophEarthbendingMaster());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        declareAttackers(player1, List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
