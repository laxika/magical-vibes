package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillmongerRuthlessUsurper.class, DarksteelMyr.class, WallOfTanglecord.class})
class KillmongerRuthlessUsurperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking scales with the defending player's artifacts and combat damage creates a Treasure")
    void attackBoostAndCombatDamageAbility() {
        Permanent killmonger = addCreatureReady(player1, new KillmongerRuthlessUsurper());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelMyr());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        Permanent remainingArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        declareAttackAndResolveTrigger();

        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();
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
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An attack dealing damage only to its blocker does not trigger the combat damage ability")
    void blockedAttackDoesNotTriggerCombatDamageAbility() {
        Permanent blocker = addCreatureReady(player2, new WallOfTanglecord());
        Permanent killmonger = addCreatureReady(player1, new KillmongerRuthlessUsurper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        declareAttackAndResolveTrigger();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(killmonger))));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Trample damage triggers sacrifice even when the blocker is indestructible")
    void trampleDamageTriggersSacrificeAndTreasure() {
        addCreatureReady(player1, new KillmongerRuthlessUsurper());
        Permanent blocker = addCreatureReady(player2, new DarksteelMyr());

        declareAttackAndResolveTrigger();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 3));
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Darksteel Myr");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage creates a Treasure even if the damaged player has no artifacts")
    void createsTreasureWithoutAnArtifactToSacrifice() {
        addCreatureReady(player1, new KillmongerRuthlessUsurper());

        declareAttackAndResolveTrigger();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Attack boost counts artifacts when the trigger resolves and leaves toughness unchanged")
    void attackBoostCountsArtifactsAtResolution() {
        Permanent killmonger = addCreatureReady(player1, new KillmongerRuthlessUsurper());
        harness.addToBattlefield(player2, new DarksteelMyr());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.addToBattlefield(player2, new DarksteelMyr());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(3);
    }

    private void declareAttackAndResolveTrigger() {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
    }
}
