package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOfTheGhastlordTest extends BaseCardTest {

    // ===== Blue enchanted creature: +1/+1 and draw on damage =====

    @Test
    @DisplayName("Blue enchanted creature dealing combat damage draws a card for its controller")
    void blueCreatureDrawsOnCombatDamage() {
        Permanent creature = addReadyCreature(player1, new FugitiveWizard());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        // Creature is not black, so the opponent does not discard.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1")
    void blueCreatureGetsBoost() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1, new FugitiveWizard()); // 1/1
        attachHelm(player1, creature);
        creature.setAttacking(true);

        resolveCombat();

        // 1/1 boosted to 2/2 deals 2 combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Black enchanted creature: +1/+1 and discard on damage =====

    @Test
    @DisplayName("Black enchanted creature dealing combat damage makes the damaged player discard")
    void blackCreatureCausesDiscardOnCombatDamage() {
        Permanent creature = addReadyCreature(player1, new ScatheZombies());
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        // Controller does not draw (creature is not blue).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }

    // ===== Creature that is neither blue nor black gets nothing =====

    @Test
    @DisplayName("Non-blue, non-black enchanted creature gets no boost and no triggered ability")
    void otherColorCreatureUnaffected() {
        harness.setLife(player2, 20);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears()); // 2/2 green
        attachHelm(player1, creature);
        creature.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // No +1/+1: 2/2 deals exactly 2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // No draw, no discard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void attachHelm(Player controller, Permanent creature) {
        Permanent helm = new Permanent(new HelmOfTheGhastlord());
        helm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(helm);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        // Pass through combat damage and the resulting triggered ability resolution.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
