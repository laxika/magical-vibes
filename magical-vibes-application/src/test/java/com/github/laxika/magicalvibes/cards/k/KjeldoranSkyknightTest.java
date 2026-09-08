package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.ShamblingStrider;
import com.github.laxika.magicalvibes.cards.s.SilverErne;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KjeldoranSkyknight.class, BalduvianBears.class, SilverErne.class,
        ShamblingStrider.class})
class KjeldoranSkyknightTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new KjeldoranSkyknight());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike kills a 2/2 blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeReciprocalDamage() {
        Permanent skyknight = addCreatureReady(player1, new KjeldoranSkyknight());
        addCreatureReady(player2, new SilverErne());
        skyknight.setPowerModifier(1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Kjeldoran Skyknight");
        harness.assertNotOnBattlefield(player2, "Silver Erne");
        harness.assertInGraveyard(player2, "Silver Erne");
    }

    @Test
    @DisplayName("Banding attacker: active player divides the blocker's combat damage")
    void bandingAttackerLetsActivePlayerDivideBlockerDamage() {
        addCreatureReady(player1, new KjeldoranSkyknight());
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

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 5));

        harness.assertOnBattlefield(player1, "Kjeldoran Skyknight");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player2, "Shambling Strider");
    }

    @Test
    @DisplayName("Banding blocker lets the defending player divide attacking damage")
    void bandingBlockerLetsDefenderDivideAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new ShamblingStrider());
        Permanent skyknight = addCreatureReady(player2, new KjeldoranSkyknight());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(bears.getId(), 5));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(skyknight);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }
}
