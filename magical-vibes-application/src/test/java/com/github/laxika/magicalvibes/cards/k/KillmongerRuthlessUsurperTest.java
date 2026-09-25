package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillmongerRuthlessUsurper.class, DarksteelMyr.class, GrizzlyBears.class})
class KillmongerRuthlessUsurperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking scales with the defending player's artifacts and combat damage creates a Treasure")
    void attackBoostAndCombatDamageAbility() {
        Permanent killmonger = addCreatureReady(player1, new KillmongerRuthlessUsurper());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelMyr());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        Permanent remainingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(killmonger)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                sacrificedArtifact.getId(), remainingArtifact.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(sacrificedArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(remainingArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sacrificedArtifact);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Blocked attacks do not trigger the artifact sacrifice or Treasure creation")
    void blockedAttackDoesNotTriggerCombatDamageAbility() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent killmonger = addCreatureReady(player1, new KillmongerRuthlessUsurper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(killmonger)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(killmonger))));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
