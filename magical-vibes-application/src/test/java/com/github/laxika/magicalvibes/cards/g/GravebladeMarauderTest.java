package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GravebladeMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player loses life equal to creature cards in the controller's graveyard")
    void damagedPlayerLosesLifePerCreatureCardInGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();

        // 1 combat damage: 20 -> 19. Two creature cards in the graveyard: 19 -> 17.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("No life loss beyond combat damage with an empty graveyard")
    void noExtraLifeLossWithEmptyGraveyard() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of());

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Only the controller's graveyard is counted, not the opponent's")
    void opponentGraveyardIsNotCounted() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No trigger when the attacker is blocked and deals no damage to a player")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent marauder = addCreatureReady(player1, new GravebladeMarauder());
        marauder.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
