package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.cards.m.MindFlayer;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeleportationCircle.class, LeatherArmor.class, DireWolfProwler.class, Plains.class, MindFlayer.class})
class TeleportationCircleTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers an artifact or creature you control at your end step")
    void flickersArtifactOrCreatureYouControl() {
        harness.addToBattlefield(player1, new TeleportationCircle());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(artifact.getId(), creature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(findPermanent(player1, "Dire Wolf Prowler").getId()).isNotEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("May choose no target")
    void mayChooseNoTarget() {
        harness.addToBattlefield(player1, new TeleportationCircle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new TeleportationCircle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent or a land")
    void cannotTargetOpponentPermanentOrLand() {
        harness.addToBattlefield(player1, new TeleportationCircle());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
    }

    @Test
    void returnsArtifactImmediatelyAsNewUntappedPermanentWithoutCounters() {
        harness.addToBattlefield(player1, new TeleportationCircle());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        artifact.tap();
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Leather Armor");
        assertThat(returned.getId()).isNotEqualTo(artifact.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(artifact.getCard().getId())).isNull();
    }

    @Test
    void returnsStolenCreatureToItsOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        harness.setHand(player1, List.of(new MindFlayer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.addToBattlefield(player1, new TeleportationCircle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dire Wolf Prowler");
        Permanent returned = findPermanent(player2, "Dire Wolf Prowler");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void doesNotFlickerTargetWhenControlIsLostBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        harness.setHand(player1, List.of(new MindFlayer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        Permanent mindFlayer = findPermanent(player1, "Mind Flayer");

        harness.addToBattlefield(player1, new TeleportationCircle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mindFlayer));
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertNotOnBattlefield(player1, "Dire Wolf Prowler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutTargetWhenNoEligiblePermanentExists() {
        harness.addToBattlefield(player1, new TeleportationCircle());
        harness.addToBattlefield(player1, new Plains());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Teleportation Circle");
        harness.assertOnBattlefield(player1, "Plains");
    }
}
