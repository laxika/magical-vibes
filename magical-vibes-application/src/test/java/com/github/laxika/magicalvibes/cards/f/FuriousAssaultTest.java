package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FuriousAssault.class, GarrukWildspeaker.class, WildJhovall.class, Tremor.class})
class FuriousAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature deals 1 damage to the chosen player")
    void creatureSpellDealsDamage() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.castFromHand(player1, new WildJhovall(), "{3}{R}");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a creature deals 1 damage to a planeswalker target")
    void creatureSpellDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new FuriousAssault());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.castFromHand(player1, new WildJhovall(), "{3}{R}");
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Furious Assault")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent casting a creature does not trigger Furious Assault")
    void opponentCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FuriousAssault());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new WildJhovall(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
