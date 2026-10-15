package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.d.DragonsHoard;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTrashmaster.class, GoblinMotivator.class, GreenwoodSentinel.class, DragonsHoard.class, BoggartShenanigans.class})
class GoblinTrashmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Goblins you control get +1/+1")
    void buffsOtherOwnGoblins() {
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinMotivator());
        harness.addToBattlefield(player1, new GoblinTrashmaster());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinMotivator());

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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonsHoard());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Trashmaster");
        harness.assertInGraveyard(player2, "Dragon's Hoard");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(trashmaster);
    }

    @Test
    @DisplayName("Sacrifice ability can choose another Goblin and leaves the source alive")
    void choosesAnotherGoblinToSacrifice() {
        Permanent trashmaster = addCreatureReady(player1, new GoblinTrashmaster());
        Permanent goblin = addCreatureReady(player1, new GoblinMotivator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonsHoard());

        harness.activateAbility(player1, 0, null, artifact.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Motivator");
        harness.assertOnBattlefield(player1, "Goblin Trashmaster");
        harness.assertInGraveyard(player2, "Dragon's Hoard");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(trashmaster);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a nonartifact")
    void cannotTargetNonartifact() {
        addCreatureReady(player1, new GoblinTrashmaster());
        Permanent nonartifact = addCreatureReady(player2, new GreenwoodSentinel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonartifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Goblin Trashmaster");
    }

    @Test
    @DisplayName("Each Trashmaster boosts the other but does not boost itself")
    void twoTrashmastersBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoblinTrashmaster());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoblinTrashmaster());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing Trashmaster immediately removes its boost from surviving Goblins")
    void sacrificingSourceRemovesBoostBeforeResolution() {
        Permanent trashmaster = harness.addToBattlefieldAndReturn(player1, new GoblinTrashmaster());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinMotivator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonsHoard());
        assertThat(gqs.getEffectivePower(gd, trashmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, trashmaster)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.handlePermanentChosen(player1, trashmaster.getId());

        harness.assertInGraveyard(player1, "Goblin Trashmaster");
        harness.assertOnBattlefield(player2, "Dragon's Hoard");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Dragon's Hoard");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Trashmaster can sacrifice itself to destroy its controller's artifact")
    void tappedNewTrashmasterCanDestroyOwnArtifact() {
        Permanent trashmaster = harness.addToBattlefieldAndReturn(player1, new GoblinTrashmaster());
        trashmaster.setSummoningSick(true);
        trashmaster.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DragonsHoard());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.assertInGraveyard(player1, "Goblin Trashmaster");
        harness.assertOnBattlefield(player1, "Dragon's Hoard");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dragon's Hoard");
    }

    @Test
    @DisplayName("A noncreature Goblin permanent can be sacrificed to destroy an artifact")
    void canSacrificeKindredGoblinEnchantment() {
        harness.addToBattlefield(player1, new GoblinTrashmaster());
        Permanent shenanigans = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DragonsHoard());

        harness.activateAbility(player1, 0, null, artifact.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(shenanigans.getId());
        harness.handlePermanentChosen(player1, shenanigans.getId());
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Goblin Trashmaster");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dragon's Hoard");
    }
}
