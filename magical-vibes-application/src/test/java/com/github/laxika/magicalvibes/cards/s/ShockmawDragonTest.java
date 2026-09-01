package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShockmawDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger deals exactly 1 damage to each creature the damaged player controls")
    void dealsOneDamageToDamagedPlayersCreatures() {
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        dragon.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears oneToughnessCard = new GrizzlyBears();
        oneToughnessCard.setToughness(1);
        Permanent oneToughnessCreature = addCreatureReady(player2, oneToughnessCard);
        Permanent twoToughnessCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oneToughnessCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(twoToughnessCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Shockmaw Dragon does not trigger when it deals no combat damage to a player")
    void doesNotTriggerWhenBlocked() {
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        dragon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damagedPlayersCreature);
        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }
}
