package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndeadExecutioner.class, GrizzlyBears.class, Vorstclaw.class, WrathOfGod.class})
class UndeadExecutionerTest extends BaseCardTest {

    /**
     * Attacks with Undead Executioner (player1) into a blocker that kills it, so the death trigger fires.
     */
    private void setupCombatWhereExecutionerDies() {
        Permanent executioner = findPermanent(player1, "Undead Executioner");
        executioner.setSummoningSick(false);
        executioner.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(4);
        Permanent blocker = addCreatureReady(player2, bigBear);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private Permanent permanentById(UUID ownerId, UUID id) {
        return harness.getGameData().playerBattlefields.get(ownerId).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Accepting the death trigger gives the chosen creature -2/-2")
    void deathTriggerGivesMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new UndeadExecutioner());

        GrizzlyBears tough = new GrizzlyBears();
        tough.setPower(2);
        tough.setToughness(3);
        harness.addToBattlefield(player2, tough);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereExecutionerDies();
        harness.passBothPriorities(); // combat damage — Executioner dies

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Undead Executioner");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities(); // resolve trigger — prompts the "you may"
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = permanentById(player2.getId(), bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isEqualTo(-2);
        assertThat(bears.getEffectivePower()).isZero();
    }

    @Test
    @DisplayName("Declining the death trigger leaves the chosen creature untouched")
    void decliningLeavesCreatureUntouched() {
        harness.addToBattlefield(player1, new UndeadExecutioner());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereExecutionerDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent bears = permanentById(player2.getId(), bearsId);
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("-2/-2 kills a 2/2 creature via state-based actions")
    void deathTriggerKillsTwoTwoCreature() {
        harness.addToBattlefield(player1, new UndeadExecutioner());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereExecutionerDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new UndeadExecutioner());

        GrizzlyBears tough = new GrizzlyBears();
        tough.setPower(4);
        tough.setToughness(4);
        harness.addToBattlefield(player2, tough);
        UUID toughId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereExecutionerDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, toughId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent target = permanentById(player2.getId(), toughId);
        assertThat(target.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Death trigger is skipped when no creature survives to be targeted")
    void deathTriggerSkippedWithNoTargets() {
        harness.addToBattlefield(player1, new UndeadExecutioner());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // resolve Wrath — every creature dies at once

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Undead Executioner");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The death trigger can target a creature its controller controls")
    void deathTriggerCanTargetOwnCreature() {
        Permanent executioner = addCreatureReady(player1, new UndeadExecutioner());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        Permanent blocker = addCreatureReady(player2, new Vorstclaw());
        executioner.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.assertInGraveyard(player1, "Undead Executioner");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("A death trigger whose target dies before resolution does not affect another creature")
    void deathTriggerDoesNotResolveWhenTargetIsGone() {
        Permanent executioner = addCreatureReady(player1, new UndeadExecutioner());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        Permanent blocker = addCreatureReady(player2, new Vorstclaw());
        executioner.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handlePermanentChosen(player1, target.getId());
        target.setToughnessModifier(-7);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Vorstclaw");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }
}
