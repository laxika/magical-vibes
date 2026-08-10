package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PearlShardTest extends BaseCardTest {

    @Test
    @DisplayName("The {3} activation prevents the next 2 damage to a player")
    void genericActivationPreventsDamageToPlayer() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        addAttacker(player2, 2, 2);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The {W} activation prevents the next 2 damage to a creature")
    void whiteActivationPreventsDamageToCreature() {
        harness.addToBattlefield(player1, new PearlShard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addAttacker(player1, 2, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private Permanent addAttacker(Player player, int power, int toughness) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setPowerModifier(power - 2);
        attacker.setToughnessModifier(toughness - 2);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }
}
