package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromokaCaptain.class, GrizzlyBears.class})
class DromokaCaptainTest extends BaseCardTest {

    @Test
    void attackingDromokaCaptainBolstersTheLeastToughCreature() {
        Permanent captain = addAttacker(new DromokaCaptain());
        Permanent bears = addAttacker(new GrizzlyBears());

        beginCombat();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackingAnotherCreatureDoesNotTriggerDromokaCaptain() {
        addAttacker(new DromokaCaptain());
        Permanent bears = addAttacker(new GrizzlyBears());

        beginCombat();
        gs.declareAttackers(gd, player1, List.of(1));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void beginCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
