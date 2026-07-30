package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

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

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — every creature dies at once

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Undead Executioner");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }
}
