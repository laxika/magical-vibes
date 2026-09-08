package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.s.ShamblingStrider;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranPhalanx.class, BalduvianBarbarians.class, BalduvianBears.class,
        ShamblingStrider.class})
class KjeldoranPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("First strike kills a 3/2 blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeReciprocalDamage() {
        Permanent phalanx = addCreatureReady(player1, new KjeldoranPhalanx());
        phalanx.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phalanx);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("Banding blocker lets the defending player divide attacking damage")
    void bandingBlockerLetsDefenderDivideAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new ShamblingStrider());
        attacker.setAttacking(true);

        Permanent phalanx = addCreatureReady(player2, new KjeldoranPhalanx());
        Permanent bear = addCreatureReady(player2, new BalduvianBears());
        phalanx.setBlocking(true);
        phalanx.addBlockingTarget(0);
        bear.setBlocking(true);
        bear.addBlockingTarget(0);

        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(bear.getId(), 5));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(phalanx);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Banding attacker: active player divides the blocker's combat damage")
    void bandingAttackerLetsActivePlayerDivideBlockerDamage() {
        Permanent phalanx = addCreatureReady(player1, new KjeldoranPhalanx());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent wall = addCreatureReady(player2, new ShamblingStrider());

        UUID band = UUID.randomUUID();
        phalanx.setAttacking(true);
        phalanx.setBandId(band);
        bears.setAttacking(true);
        bears.setBandId(band);

        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        wall.addBlockingTarget(1);

        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(5);

        // Funnel all 5 damage onto the bears; first strike from Phalanx already resolved.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 5));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phalanx);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
    }
}
