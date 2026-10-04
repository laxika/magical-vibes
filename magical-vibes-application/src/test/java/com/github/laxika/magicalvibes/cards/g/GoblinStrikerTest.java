package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuriokTransfixer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinStriker.class, AuriokTransfixer.class})
class GoblinStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackTheTurnItEnters() {
        harness.addToBattlefieldAndReturn(player1, new GoblinStriker());

        declareAttackers(List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("First strike deals combat damage before an equal-sized blocker")
    void firstStrikeDealsDamageFirst() {
        Permanent striker = addCreatureReady(player1, new GoblinStriker());
        striker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AuriokTransfixer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Goblin Striker");
        harness.assertInGraveyard(player2, "Auriok Transfixer");
    }

    @Test
    @DisplayName("First strike kills an equal-sized attacker before it can deal damage")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new AuriokTransfixer());
        attacker.setAttacking(true);

        Permanent striker = harness.addToBattlefieldAndReturn(player2, new GoblinStriker());
        striker.setBlocking(true);
        striker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Auriok Transfixer");
        harness.assertOnBattlefield(player2, "Goblin Striker");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Equal-sized creatures with first strike deal lethal damage simultaneously")
    void bothFirstStrikersDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new GoblinStriker());
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GoblinStriker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Goblin Striker");
        harness.assertInGraveyard(player2, "Goblin Striker");
        harness.assertLife(player2, 20);
    }
}
