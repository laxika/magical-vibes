package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrontierMastodon;
import com.github.laxika.magicalvibes.cards.m.MindscourDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShockmawDragon.class, FrontierMastodon.class, SandsteppeOutcast.class, MindscourDragon.class})
class ShockmawDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger deals exactly 1 damage to each creature the damaged player controls")
    void dealsOneDamageToDamagedPlayersCreatures() {
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        dragon.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new FrontierMastodon());
        Permanent oneToughnessCreature = addCreatureReady(player2, new SandsteppeOutcast());
        Permanent twoToughnessCreature = addCreatureReady(player2, new FrontierMastodon());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oneToughnessCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(twoToughnessCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(twoToughnessCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Shockmaw Dragon does not trigger when it deals no combat damage to a player")
    void doesNotTriggerWhenBlocked() {
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        dragon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MindscourDragon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new FrontierMastodon());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damagedPlayersCreature);
        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage trigger still deals damage after Shockmaw Dragon leaves the battlefield")
    void triggerResolvesAfterDragonLeaves() {
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        dragon.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new SandsteppeOutcast());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dragon));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Shockmaw Dragon");
        harness.assertInGraveyard(player2, "Sandsteppe Outcast");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Combat damage trigger affects creatures controlled at resolution, including new creatures")
    void affectsCreaturesPresentAtResolution() {
        Permanent dragon = addCreatureReady(player2, new ShockmawDragon());
        dragon.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.assertLife(player1, 16);
        assertThat(gd.stack).hasSize(1);

        Permanent newCreature = addCreatureReady(player1, new SandsteppeOutcast());
        Permanent ownCreature = addCreatureReady(player2, new FrontierMastodon());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(newCreature);
        harness.assertInGraveyard(player1, "Sandsteppe Outcast");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }
}
