package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngryMob.class, Swamp.class})
class AngryMobTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, P/T is 2 plus the Swamps your opponents control")
    void yourTurnAddsOpponentSwamps() {
        Permanent mob = addCreatureReady(player1, new AngryMob());
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(4);
    }

    @Test
    @DisplayName("During your turn with no opponent Swamps, P/T is 2")
    void yourTurnNoOpponentSwamps() {
        Permanent mob = addCreatureReady(player1, new AngryMob());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("During an opponent's turn, P/T is a flat 2 regardless of Swamps")
    void opponentTurnIsFlatTwo() {
        Permanent mob = addCreatureReady(player1, new AngryMob());
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only opponents' Swamps count, not your own")
    void ignoresControllersOwnSwamps() {
        Permanent mob = addCreatureReady(player1, new AngryMob());
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mob)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mob)).isEqualTo(3);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new AngryMob());
        Permanent blocker = addCreatureReady(player2, new AngryMob());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

}
