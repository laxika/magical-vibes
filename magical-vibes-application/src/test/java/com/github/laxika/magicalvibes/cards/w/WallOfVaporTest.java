package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfVapor.class, DAvenantArcher.class, PsychicPurge.class})
class WallOfVaporTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature Wall of Vapor blocks is prevented")
    void preventsCombatDamageFromBlockedCreature() {
        addCreatureReady(player1, new DAvenantArcher());
        Permanent wall = addCreatureReady(player2, new WallOfVapor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage from a creature Wall of Vapor blocks is prevented")
    void preventsNoncombatDamageFromBlockedCreature() {
        Permanent wall = addCreatureReady(player1, new WallOfVapor());
        Permanent archer = addCreatureReady(player2, new DAvenantArcher());
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        wall.addBlockingTargetId(archer.getId());

        harness.activateAbility(player2, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage from creatures Wall of Vapor does not block is not prevented")
    void doesNotPreventDamageFromOtherCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfVapor());
        addCreatureReady(player1, new DAvenantArcher());
        Permanent blockedCreature = addCreatureReady(player1, new DAvenantArcher());
        wall.setBlocking(true);
        wall.addBlockingTarget(1);
        wall.addBlockingTargetId(blockedCreature.getId());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Vapor");
    }

    @Test
    @DisplayName("Damage from a noncreature source is not prevented")
    void doesNotPreventDamageFromNoncreatureSource() {
        Permanent wall = addCreatureReady(player2, new WallOfVapor());
        harness.setHand(player1, List.of(new PsychicPurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertNotOnBattlefield(player2, "Wall of Vapor");
    }
}
