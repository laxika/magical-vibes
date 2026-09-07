package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.ShamblingStrider;
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

@CardUsed({KjeldoranWarrior.class, BalduvianBears.class, ShamblingStrider.class})
class KjeldoranWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Banding attacker: active player divides the blocker's combat damage")
    void bandingAttackerLetsActivePlayerDivideBlockerDamage() {
        addCreatureReady(player1, new KjeldoranWarrior());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player2, new ShamblingStrider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 5));

        harness.assertOnBattlefield(player1, "Kjeldoran Warrior");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player2, "Shambling Strider");
    }

    @Test
    @DisplayName("Banding blocker: defending player divides the attacker's combat damage")
    void bandingBlockerLetsDefendingPlayerDivideAttackerDamage() {
        addCreatureReady(player1, new ShamblingStrider());
        Permanent warrior = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt = gd.interaction
                .activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(bears.getId(), 5));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(findPermanent(player1, "Shambling Strider"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(warrior);
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
    }
}
