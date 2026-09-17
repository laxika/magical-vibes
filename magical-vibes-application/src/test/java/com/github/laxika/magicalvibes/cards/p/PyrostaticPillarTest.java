package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Dragonstalker;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.g.GoblinWarchief;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyrostaticPillar.class, GoblinWarchief.class, GoblinBrigand.class, Dragonstalker.class})
class PyrostaticPillarTest extends BaseCardTest {

    @Test
    void dealsDamageToOpponentWhenTheyCastSpellWithManaValueThreeOrLess() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GoblinWarchief(), "{1}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsDamageToControllerWhenTheyCastSpellWithManaValueThreeOrLess() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new GoblinBrigand(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void doesNotTriggerForSpellWithManaValueGreaterThanThree() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Dragonstalker(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
