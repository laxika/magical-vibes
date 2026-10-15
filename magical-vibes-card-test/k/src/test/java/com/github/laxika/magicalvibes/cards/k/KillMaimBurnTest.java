package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CanoptekScarabSwarm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KillMaimBurn.class, FountainOfYouth.class, GrizzlyBears.class, CanoptekScarabSwarm.class})
class KillMaimBurnTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode destroys an artifact")
    void destroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        cast(new int[]{0}, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Creature mode destroys a creature")
    void destroysCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Player mode deals 3 damage to a player")
    void damagesPlayer() {
        cast(new int[]{2}, List.of(player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Choosing all modes resolves each effect")
    void resolvesAllModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0, 1, 2}, List.of(artifact.getId(), creature.getId(), player2.getId()));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Modes reject targets of the wrong type")
    void rejectsWrongTargetType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KillMaimBurn()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player mode can damage its controller")
    void damagesController() {
        cast(new int[]{2}, List.of(player1.getId()));

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Artifact and creature modes resolve without dealing player damage")
    void resolvesBothDestructionModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0, 1}, List.of(artifact.getId(), creature.getId()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Artifact and player modes leave an unselected creature alone")
    void resolvesArtifactAndPlayerModes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast(new int[]{0, 2}, List.of(artifact.getId(), player2.getId()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Creature and player modes leave an unselected artifact alone")
    void resolvesCreatureAndPlayerModes() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{1, 2}, List.of(creature.getId(), player2.getId()));

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An artifact creature can be targeted by both destruction modes")
    void sameArtifactCreatureCanBeTargetedByBothModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CanoptekScarabSwarm());
        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        harness.assertNotOnBattlefield(player2, "Canoptek Scarab Swarm");
        harness.assertInGraveyard(player2, "Canoptek Scarab Swarm");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Losing the artifact target does not prevent creature destruction or player damage")
    void remainingModesResolveWhenArtifactTargetLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KillMaimBurn()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(artifact.getId(), creature.getId(), player2.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.setGraveyard(player2, List.of(artifact.getCard()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Kill! Maim! Burn!");
    }

    @Test
    @DisplayName("Creature mode rejects a noncreature artifact")
    void creatureModeRejectsNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new KillMaimBurn()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{1}, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player mode rejects a creature")
    void playerModeRejectsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KillMaimBurn()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{2}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new KillMaimBurn()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 3, modes, targets);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
