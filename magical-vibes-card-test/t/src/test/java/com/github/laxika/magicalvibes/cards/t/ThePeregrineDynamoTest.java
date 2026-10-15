package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArcanisTheOmnipotent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.l.LithoformEngine;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RekiTheHistoryOfKamigawa;
import com.github.laxika.magicalvibes.cards.y.YidaroWanderingMonster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThePeregrineDynamo.class, RekiTheHistoryOfKamigawa.class,
        TheOzolith.class, ProdigalPyromancer.class, GrizzlyBears.class,
        ArcanisTheOmnipotent.class, LithoformEngine.class, YidaroWanderingMonster.class,
        KamahlPitFighter.class})
class ThePeregrineDynamoTest extends BaseCardTest {

    @Test
    void copiesAnAbilityFromAnotherLegendaryNoncommanderSource() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new RekiTheHistoryOfKamigawa());
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TheOzolith(), "{1}");

        UUID rekiTriggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.activateAbility(player1, 1, null, rekiTriggerId);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotTargetAnAbilityFromANonlegendarySource() {
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pyromancerAbilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAnAbilityFromACommanderSource() {
        Permanent reki = harness.addToBattlefieldAndReturn(player1, new RekiTheHistoryOfKamigawa());
        reki.setCommander(true);
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new TheOzolith(), "{1}");

        UUID rekiTriggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, rekiTriggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiesActivatedAbilityWithoutPayingItsCostAgain() {
        addCreatureReady(player1, new ArcanisTheOmnipotent());
        Permanent dynamo = harness.addToBattlefieldAndReturn(player1, new ThePeregrineDynamo());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 1, null, abilityId);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(dynamo.isTapped()).isTrue();
    }

    @Test
    void cannotTargetOpponentsLegendaryAbility() {
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        addCreatureReady(player2, new ArcanisTheOmnipotent());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCopyLegendaryCyclingAbilityFromOutsideTheBattlefield() {
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.setHand(player1, List.of(new YidaroWanderingMonster()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        UUID cyclingId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, cyclingId);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    void cannotCopyCyclingAbilityOfACommander() {
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        YidaroWanderingMonster commander = new YidaroWanderingMonster();
        harness.setHand(player1, List.of(commander));
        gd.makeCommander(player1.getId(), commander);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.runStateBasedActions();
        harness.handleMayAbilityChosen(player1, false);
        UUID cyclingId = gd.stack.getLast().getTargetableId();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cyclingId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetACopyOfItsOwnAbility() {
        addCreatureReady(player1, new ArcanisTheOmnipotent());
        Permanent dynamo = harness.addToBattlefieldAndReturn(player1, new ThePeregrineDynamo());
        harness.addToBattlefield(player1, new LithoformEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        UUID arcanisId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 1, null, arcanisId);
        UUID dynamoId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 2, 0, null, dynamoId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        UUID copiedDynamoId = gd.stack.stream()
                .filter(StackEntry::isCopy)
                .findFirst().orElseThrow().getTargetableId();
        dynamo.untap();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, copiedDynamoId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayChooseANewTargetForTheCopy() {
        harness.addToBattlefield(player1, new KamahlPitFighter());
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 1, null, abilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void mayKeepTheOriginalTargetForTheCopy() {
        harness.addToBattlefield(player1, new KamahlPitFighter());
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 1, null, abilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void stillCopiesTheAbilityAfterItsSourceLeavesTheBattlefield() {
        addCreatureReady(player1, new ArcanisTheOmnipotent());
        harness.addToBattlefield(player1, new ThePeregrineDynamo());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        UUID drawId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 1, null, drawId);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Arcanis the Omnipotent");
        harness.assertInHand(player1, "Arcanis the Omnipotent");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }
}
