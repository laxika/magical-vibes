package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogardanFirefiend.class, FountainOfYouth.class, GrizzlyBears.class, WrathOfGod.class})
class BogardanFirefiendTest extends BaseCardTest {

    /**
     * Sets up combat where Bogardan Firefiend (player1) attacks and is blocked by a 3/3 creature (player2).
     * Firefiend (2/1) will die from combat damage.
     */
    private void setupCombatWhereFirefiendDies() {
        Permanent firefiendPerm = findPermanent(player1, "Bogardan Firefiend");
        firefiendPerm.setSummoningSick(false);
        firefiendPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = addCreatureReady(player2, bigBear);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Bogardan Firefiend puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new BogardanFirefiend(), "{2}{R}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bogardan Firefiend");
    }

    // ===== Death trigger with target selection =====

    @Test
    @DisplayName("When Bogardan Firefiend dies in combat, controller is prompted to choose a target creature")
    void deathTriggerPromptsForTarget() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereFirefiendDies();

        resolveCombat();

        // Bogardan Firefiend should be dead
        harness.assertInGraveyard(player1, "Bogardan Firefiend");

        // Player1 should be prompted to choose a target creature
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger deals 2 damage to chosen creature and destroys it if lethal")
    void deathTriggerDeals2DamageAndKills() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the surviving Grizzly Bears (2/2)
        harness.handlePermanentChosen(player1, bearsId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bogardan Firefiend");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        // Resolve the triggered ability — 2 damage to a 2/2 is lethal
        harness.passBothPriorities();

        // Grizzly Bears should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger deals 2 damage but does not kill a creature with toughness > 2")
    void deathTriggerDoesNotKillHighToughness() {
        harness.addToBattlefield(player1, new BogardanFirefiend());

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        harness.addToBattlefield(player2, bigBear);

        UUID bigBearId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the 3/3 bear
        harness.handlePermanentChosen(player1, bigBearId);

        // Resolve the triggered ability — 2 damage to a 3/3 is not lethal
        harness.passBothPriorities();

        // Grizzly Bears should still be alive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bigBearId));
    }

    @Test
    @DisplayName("Death trigger can target own creature")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose own Grizzly Bears
        harness.handlePermanentChosen(player1, ownBearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Own Grizzly Bears (2/2) should be destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownBearsId));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death trigger only offers creature permanents as targets")
    void deathTriggerOnlyOffersCreatureTargets() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        setupCombatWhereFirefiendDies();
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(artifact.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Each Firefiend that dies creates its own damage trigger")
    void eachDyingFirefiendCreatesDamageTrigger() {
        Permanent firstFirefiend = harness.addToBattlefieldAndReturn(player1, new BogardanFirefiend());
        Permanent secondFirefiend = harness.addToBattlefieldAndReturn(player1, new BogardanFirefiend());
        firstFirefiend.setSummoningSick(false);
        firstFirefiend.setAttacking(true);
        secondFirefiend.setSummoningSick(false);
        secondFirefiend.setAttacking(true);

        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        GrizzlyBears firstBlockerCard = new GrizzlyBears();
        firstBlockerCard.setPower(3);
        firstBlockerCard.setToughness(3);
        Permanent firstBlocker = addCreatureReady(player2, firstBlockerCard);
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);

        GrizzlyBears secondBlockerCard = new GrizzlyBears();
        secondBlockerCard.setPower(3);
        secondBlockerCard.setToughness(3);
        Permanent secondBlocker = addCreatureReady(player2, secondBlockerCard);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(1);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Bogardan Firefiend"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Death trigger skips if no creatures are on the battlefield (Wrath of God)")
    void deathTriggerSkipsWithNoCreatures() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Use Wrath of God to kill all creatures simultaneously
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        // Both creatures should be dead
        harness.assertNotOnBattlefield(player1, "Bogardan Firefiend");
        harness.assertInGraveyard(player1, "Bogardan Firefiend");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No permanent choice should be prompted (no valid creature targets after Wrath)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        // Log should mention no valid targets
        assertThat(gameLogContains("no valid targets")).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

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
    @DisplayName("Death trigger puts triggered ability on the stack with correct target")
    void deathTriggerPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new BogardanFirefiend());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereFirefiendDies();
        resolveCombat();

        // Choose target
        harness.handlePermanentChosen(player1, bearsId);

        // Ability should be on the stack with correct attributes
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Bogardan Firefiend")
                && e.getTargetId().equals(bearsId));
    }
}

