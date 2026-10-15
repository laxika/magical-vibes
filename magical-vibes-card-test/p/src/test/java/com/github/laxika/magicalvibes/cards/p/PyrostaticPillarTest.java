package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Carbonize;
import com.github.laxika.magicalvibes.cards.d.Dragonstalker;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.g.GoblinWarchief;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.s.SiegeGangCommander;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyrostaticPillar.class, GoblinWarchief.class, GoblinBrigand.class, Dragonstalker.class,
        Carbonize.class, ScornfulEgotist.class, SiegeGangCommander.class})
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

    @Test
    void doesNotTriggerForItsOwnCasting() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new PyrostaticPillar(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Pyrostatic Pillar");
    }

    @Test
    void eachPillarDealsDamageIndependently() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.addToBattlefield(player2, new PyrostaticPillar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new GoblinBrigand(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggersBeforeAnInstantResolves() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Carbonize()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void triggersForFaceDownSpellRegardlessOfPrintedManaValue() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void costReductionDoesNotLowerSpellManaValue() {
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.addToBattlefield(player1, new GoblinWarchief());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new SiegeGangCommander(), "{1}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Siege-Gang Commander");
    }
}
