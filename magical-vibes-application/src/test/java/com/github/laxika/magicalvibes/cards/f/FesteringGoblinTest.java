package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FesteringGoblin.class, GrizzlyBears.class, WrathOfGod.class, MindStone.class})
class FesteringGoblinTest extends BaseCardTest {

    /** Sets up and resolves combat where Festering Goblin attacks into a 3/3 blocker. */
    private void resolveCombatWhereGoblinDies() {
        Permanent goblinPerm = findPermanent(player1, "Festering Goblin");
        goblinPerm.setSummoningSick(false);
        goblinPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = addCreatureReady(player2, bigBear);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        resolveCombat();
    }

    @Test
    @DisplayName("When Festering Goblin dies in combat, controller is prompted to choose a target creature")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        resolveCombatWhereGoblinDies();

        // Festering Goblin should be dead
        harness.assertInGraveyard(player1, "Festering Goblin");

        // Player1 should be prompted to choose a target creature
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger gives -1/-1 to chosen creature after resolution")
    void deathTriggerTargetsOpponentCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombatWhereGoblinDies();

        // Player1 should be prompted to choose a target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the surviving Grizzly Bears
        harness.handlePermanentChosen(player1, bearsId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Festering Goblin");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Grizzly Bears should have -1/-1
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger can target own creature")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        resolveCombatWhereGoblinDies();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose own Grizzly Bears
        harness.handlePermanentChosen(player1, ownBearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Grizzly Bears should have -1/-1
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Death trigger killing a 1/1 creature sends it to graveyard after SBA")
    void deathTriggerKillsOneOneCreature() {
        harness.addToBattlefield(player1, new FesteringGoblin());

        // Put a 1/1 creature for player2
        GrizzlyBears weakBear = new GrizzlyBears();
        weakBear.setPower(1);
        weakBear.setToughness(1);
        harness.addToBattlefield(player2, weakBear);

        UUID weakBearId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombatWhereGoblinDies();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the 1/1 bear
        harness.handlePermanentChosen(player1, weakBearId);

        // Resolve the triggered ability — bear gets -1/-1 (now 0/0)
        harness.passBothPriorities();

        // The weak bear should now be dead from SBA (0 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(weakBearId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombatWhereGoblinDies();

        // Choose target and resolve
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        // Bear should have -1/-1 now
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(-1);

        // Advance to cleanup step — modifiers reset
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Death trigger skips if no creatures are on the battlefield (Wrath of God)")
    void deathTriggerSkipsWithNoCreatures() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Use Wrath of God to kill all creatures simultaneously
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        // Both creatures should be dead
        harness.assertNotOnBattlefield(player1, "Festering Goblin");
        harness.assertInGraveyard(player1, "Festering Goblin");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No permanent choice should be prompted (no valid creature targets after Wrath)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        // Log should mention no valid targets
        assertThat(gameLogContains("no valid targets")).isTrue();
    }

    @Test
    @DisplayName("Death trigger only offers creature permanents as targets")
    void deathTriggerCannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new MindStone());

        UUID mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        resolveCombatWhereGoblinDies();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThat(choice.validPermanentIds()).contains(creatureId);
        assertThat(choice.validPermanentIds()).doesNotContain(mindStoneId);

        harness.handlePermanentChosen(player1, creatureId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombatWhereGoblinDies();

        // Choose target
        harness.handlePermanentChosen(player1, bearsId);

        // Remove the target before the ability resolves
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getId().equals(bearsId));

        // Resolve — should fizzle
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Death trigger is put on the stack when another creature survives")
    void deathTriggerIsPutOnStackWhenAnotherCreatureSurvives() {
        harness.addToBattlefield(player1, new FesteringGoblin());
        GrizzlyBears survivor = new GrizzlyBears();
        harness.addToBattlefield(player2, survivor);

        UUID survivorId = harness.getPermanentId(player2, "Grizzly Bears");

        resolveCombatWhereGoblinDies();

        // The survivor is a valid target for the triggered ability.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the survivor
        harness.handlePermanentChosen(player1, survivorId);

        // Ability should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Festering Goblin")
                && e.getTargetId().equals(survivorId));
    }
}

