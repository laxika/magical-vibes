package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinTrashmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Goblins you control get +1/+1")
    void buffsOtherOwnGoblins() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new GoblinTrashmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RagingGoblin());

        Permanent ownGoblin = findPermanent(player1, "Raging Goblin");
        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opponentGoblin = findPermanent(player2, "Raging Goblin");

        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Goblin Trashmaster itself destroys a target artifact")
    void sacrificesItselfToDestroyArtifact() {
        Permanent trashmaster = addCreatureReady(player1, new GoblinTrashmaster());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JalumTome());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Trashmaster");
        harness.assertInGraveyard(player2, "Jalum Tome");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trashmaster);
    }

    @Test
    @DisplayName("Sacrifice ability can choose another Goblin and leaves the source alive")
    void choosesAnotherGoblinToSacrifice() {
        Permanent trashmaster = addCreatureReady(player1, new GoblinTrashmaster());
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JalumTome());

        harness.activateAbility(player1, 0, null, artifact.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Goblin Trashmaster");
        harness.assertInGraveyard(player2, "Jalum Tome");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(trashmaster);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a nonartifact")
    void cannotTargetNonartifact() {
        addCreatureReady(player1, new GoblinTrashmaster());
        Permanent nonartifact = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonartifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Goblin Trashmaster");
    }
}
