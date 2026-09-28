package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongTheBrutishThespian.class, Shock.class, GrizzlyBears.class, Forest.class})
class StrongTheBrutishThespianTest extends BaseCardTest {

    @Test
    @DisplayName("Being dealt damage gives three rad counters and three +1/+1 counters")
    void damageTriggersEnrage() {
        Permanent strong = harness.addToBattlefieldAndReturn(player2, new StrongTheBrutishThespian());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, strong.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(strong.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Radiation causes Strong's controller to gain life instead of lose life")
    void radiationCausesLifeGain() {
        harness.addToBattlefield(player1, new StrongTheBrutishThespian());
        harness.setLife(player1, 20);
        gd.playerRadCounters.put(player1.getId(), 2);
        List<com.github.laxika.magicalvibes.model.Card> library =
                List.of(new GrizzlyBears(), new Forest());
        harness.setLibrary(player1, library);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerRadCounters.get(player1.getId())).isOne();
    }
}
