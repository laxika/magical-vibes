package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrideOfLions;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LatullasOrders.class, GrizzlyBears.class, Spellbook.class, Forest.class,
        PrideOfLions.class, SpinedWurm.class})
class LatullasOrdersTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage requires an artifact target before resolution")
    void combatDamagePresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting the choice destroys an artifact controlled by the damaged player")
    void acceptingChoiceDestroysDamagedPlayersArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Only artifacts controlled by the damaged player are valid choices")
    void onlyDamagedPlayersArtifactsAreValid() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enemyForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyArtifact.getId())
                .doesNotContain(ownArtifact.getId(), enemyForest.getId());
    }

    @Test
    @DisplayName("Declining the choice leaves the artifact on the battlefield")
    void decliningChoiceLeavesArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No trigger occurs when the enchanted creature deals no combat damage to the player")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Spellbook());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The trigger survives when the enchanted creature deals damage and dies in combat")
    void triggerSurvivesEnchantedCreatureDyingInCombat() {
        Permanent creature = addCreatureReady(player1, new PrideOfLions());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        Permanent blocker = addCreatureReady(player2, new SpinedWurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        harness.assertInGraveyard(player1, "Pride of Lions");
        harness.assertInGraveyard(player1, "Latulla's Orders");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("The Aura controller chooses the target when an opposing creature deals combat damage")
    void auraControllerChoosesTargetForOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("An artifact that changes controllers before resolution is no longer a legal target")
    void changedControllerMakesTargetIllegal() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertNotInGraveyard(player2, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting and attaching the Aura during the opponent's turn")
    void castsAuraDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LatullasOrders()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Latulla's Orders").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("No artifact target means the triggered ability cannot remain on the stack")
    void noArtifactsMeansNoResolvableTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        harness.addToBattlefield(player1, new Spellbook());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Spellbook");
    }
    private void attachLatullasOrders(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new LatullasOrders());
        aura.setAttachedTo(creature.getId());
    }
}
