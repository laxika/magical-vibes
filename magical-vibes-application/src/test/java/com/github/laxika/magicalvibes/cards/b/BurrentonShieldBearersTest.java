package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BurrentonShieldBearersTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger valid targets are creatures, not players")
    void attackTriggerTargetsCreatures() {
        addReadyShieldBearers(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bearsId)
                .doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Attack trigger gives target creature +0/+3 until end of turn")
    void attackTriggerBoostsTargetCreature() {
        addReadyShieldBearers(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanentById(player2, bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target the attacking creature itself")
    void canTargetSelf() {
        Permanent bearer = addReadyShieldBearers(player1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bearer.getId());
        harness.passBothPriorities();

        assertThat(bearer.getToughnessModifier()).isEqualTo(3);
        assertThat(bearer.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadyShieldBearers(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanentById(player2, bearsId);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);

        // Combat left a blocker-declaration interaction pending (player2 has an untapped creature);
        // clear it so priority can pass through the end step into cleanup.
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadyShieldBearers(Player player) {
        Permanent perm = new Permanent(new BurrentonShieldBearers());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent permanentById(Player player, UUID id) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
