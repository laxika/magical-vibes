package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GoblinStriker;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustmouthOgre.class, Bonesplitter.class, GoblinStriker.class})
class RustmouthOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage presents an optional artifact destruction")
    void combatDamagePresentsMayChoice() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        resolveCombat();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Bonesplitter");
        harness.assertInGraveyard(player2, "Bonesplitter");
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Only artifacts controlled by the damaged player are valid choices")
    void onlyDamagedPlayersArtifactsAreValid() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        Permanent enemyCreature = addCreatureReady(player2, new GoblinStriker());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyArtifact.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("Declining the may ability leaves the artifact on the battlefield")
    void declineDestroyingArtifact() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Bonesplitter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No trigger occurs when the damaged player controls no artifacts")
    void noTriggerWithoutArtifacts() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocker does not trigger artifact destruction")
    void noTriggerWhenBlocked() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoblinStriker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.addToBattlefield(player2, new Bonesplitter());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Bonesplitter");
    }
}
