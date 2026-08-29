package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.o.Orgg;
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

@CardUsed({IcatianPhalanx.class, IcatianJavelineers.class, Orgg.class})
class IcatianPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Banding attacker lets the active player assign blocker damage")
    void bandingAttackerLetsActivePlayerAssignBlockerDamage() {
        Permanent phalanx = addCreatureReady(player1, new IcatianPhalanx());
        Permanent javelineers = addCreatureReady(player1, new IcatianJavelineers());
        Permanent orgg = addCreatureReady(player2, new Orgg());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(orgg.getBlockingTargetIds()).containsExactlyInAnyOrder(phalanx.getId(), javelineers.getId());

        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(6);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(javelineers.getId(), 6));

        harness.assertOnBattlefield(player1, "Icatian Phalanx");
        harness.assertInGraveyard(player1, "Icatian Javelineers");
        harness.assertOnBattlefield(player2, "Orgg");
    }

    @Test
    @DisplayName("Banding blocker lets the defending player assign attacker damage")
    void bandingBlockerLetsDefenderAssignAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());
        Permanent javelineers = addCreatureReady(player2, new IcatianJavelineers());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(2);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(javelineers.getId(), 2));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertOnBattlefield(player2, "Icatian Phalanx");
        harness.assertInGraveyard(player2, "Icatian Javelineers");
    }
}
