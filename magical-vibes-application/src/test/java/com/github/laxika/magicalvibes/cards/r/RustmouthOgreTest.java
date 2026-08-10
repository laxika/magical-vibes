package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RustmouthOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage presents an optional artifact destruction")
    void combatDamagePresentsMayChoice() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Only artifacts controlled by the damaged player are valid choices")
    void onlyDamagedPlayersArtifactsAreValid() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyArtifact.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("Declining the may ability leaves the artifact on the battlefield")
    void declineDestroyingArtifact() {
        Permanent ogre = addCreatureReady(player1, new RustmouthOgre());
        ogre.setAttacking(true);
        harness.addToBattlefield(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
