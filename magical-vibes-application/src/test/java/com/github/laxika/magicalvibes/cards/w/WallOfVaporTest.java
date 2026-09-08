package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfVaporTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature Wall of Vapor blocks is prevented")
    void preventsCombatDamageFromBlockedCreature() {
        Permanent wall = addCreatureReady(player1, new WallOfVapor());
        Permanent attacker = addCreatureReady(player2, new ZuranSpellcaster());
        attacker.setAttacking(true);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        wall.addBlockingTargetId(attacker.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage from a creature Wall of Vapor blocks is prevented")
    void preventsNoncombatDamageFromBlockedCreature() {
        Permanent wall = addCreatureReady(player1, new WallOfVapor());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());
        wall.setBlocking(true);
        wall.addBlockingTargetId(spellcaster.getId());

        harness.activateAbility(player2, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage from creatures Wall of Vapor does not block is not prevented")
    void doesNotPreventDamageFromOtherCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfVapor());
        addCreatureReady(player1, new ZuranSpellcaster());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Vapor");
    }

    @Test
    @DisplayName("Damage from a noncreature source is not prevented")
    void doesNotPreventDamageFromNoncreatureSource() {
        Permanent wall = addCreatureReady(player2, new WallOfVapor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Vapor");
    }
}
