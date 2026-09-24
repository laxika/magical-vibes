package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphsBravado.class, GrizzlyBears.class})
class RaphsBravadoTest extends BaseCardTest {

    @Test
    @DisplayName("During its controller's turn, Raph's Bravado boosts all attacking creatures")
    void boostsAllAttackingCreaturesDuringControllersTurn() {
        harness.addToBattlefield(player1, new RaphsBravado());
        Permanent ownAttacker = addAttackingBears(player1);
        Permanent opponentAttacker = addAttackingBears(player2);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Raph's Bravado does not boost attackers during an opponent's turn")
    void doesNotBoostDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RaphsBravado());
        Permanent ownAttacker = addAttackingBears(player1);
        Permanent opponentAttacker = addAttackingBears(player2);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(2);
    }

    @Test
    void bonusIsRemovedWhenSourceLeaves() {
        Permanent bravado = harness.addToBattlefieldAndReturn(player1, new RaphsBravado());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(bravado);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    private Permanent addAttackingBears(com.github.laxika.magicalvibes.model.Player controller) {
        Permanent creature = addCreatureReady(controller, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }
}
