package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DromokaTheEternalTest extends BaseCardTest {

    @Test
    void attackingDragonBolstersTheLeastToughCreature() {
        addAttacker(new DromokaTheEternal(), player1.getId());
        Permanent bears = addAttacker(new GrizzlyBears(), player1.getId());

        beginCombat(player1);
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackingNonDragonDoesNotTrigger() {
        addAttacker(new DromokaTheEternal(), player1.getId());
        Permanent bears = addAttacker(new GrizzlyBears(), player1.getId());

        beginCombat(player1);
        gs.declareAttackers(gd, player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsDragonDoesNotTrigger() {
        addAttacker(new DromokaTheEternal(), player1.getId());
        Permanent dragon = addAttacker(new ShivanDragon(), player2.getId());

        beginCombat(player2);
        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card, java.util.UUID owner) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(owner).add(permanent);
        return permanent;
    }

    private void beginCombat(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
