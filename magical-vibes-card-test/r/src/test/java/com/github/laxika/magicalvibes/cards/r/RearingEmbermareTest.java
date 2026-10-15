package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rearing Embermare")
@CardUsed({RearingEmbermare.class, CloudSprite.class})
class RearingEmbermareTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        harness.setHand(player1, List.of(new RearingEmbermare()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Can block a flying attacker")
    void canBlockFlyingAttacker() {
        Permanent attacker = addCreatureReady(player1, new CloudSprite());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new RearingEmbermare());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block a flying attacker while summoning sick")
    void canBlockFlyingWhileSummoningSick() {
        Permanent attacker = addCreatureReady(player1, new CloudSprite());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RearingEmbermare());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach does not restrict blocking ground attackers")
    void canBlockGroundAttacker() {
        Permanent attacker = addCreatureReady(player1, new RearingEmbermare());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RearingEmbermare());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
