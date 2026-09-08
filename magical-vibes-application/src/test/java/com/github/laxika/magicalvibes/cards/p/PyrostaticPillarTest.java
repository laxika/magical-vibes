package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyrostaticPillar.class, AirElemental.class, BottleGnomes.class, GrizzlyBears.class})
class PyrostaticPillarTest extends BaseCardTest {

    @Test
    void dealsDamageToOpponentWhenTheyCastSpellWithManaValueThreeOrLess() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new BottleGnomes()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsDamageToControllerWhenTheyCastSpellWithManaValueThreeOrLess() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void doesNotTriggerForSpellWithManaValueGreaterThanThree() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
