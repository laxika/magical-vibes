package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LatullasOrdersTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage presents a may-destroy-artifact choice")
    void combatDamagePresentsMayChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the choice destroys an artifact controlled by the damaged player")
    void acceptingChoiceDestroysDamagedPlayersArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

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
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyArtifact.getId())
                .doesNotContain(ownArtifact.getId(), enemyForest.getId());
    }

    @Test
    @DisplayName("Declining the choice leaves the artifact on the battlefield")
    void decliningChoiceLeavesArtifact() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachLatullasOrders(player1, creature);
        creature.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
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

    private void attachLatullasOrders(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new LatullasOrders());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
