package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PearlShard.class, Frogmite.class})
class PearlShardTest extends BaseCardTest {

    @Test
    @DisplayName("The {3} activation prevents the next 2 damage to a player")
    void genericActivationPreventsDamageToPlayer() {
        Permanent pearlShard = harness.addToBattlefieldAndReturn(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, player1.getId());
        assertThat(pearlShard.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Frogmite());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The prevention shield only prevents the next 2 damage")
    void preventionShieldOnlyPreventsNextTwoDamage() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Frogmite());
        attacker.setPowerModifier(1);
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The {W} activation prevents the next 2 damage to a creature")
    void whiteActivationPreventsDamageToCreature() {
        Permanent pearlShard = harness.addToBattlefieldAndReturn(player1, new PearlShard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = addCreatureReady(player2, new Frogmite());
        Permanent attacker = addCreatureReady(player1, new Frogmite());
        attacker.setAttacking(true);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(pearlShard.isTapped()).isTrue();
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}
