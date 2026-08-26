package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisingPopulace.class, AjaniGoldmane.class, GrizzlyBears.class, Shock.class})
class RisingPopulaceTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature you control dies")
    void getsCounterWhenAllyCreatureDies() {
        Permanent populace = harness.addToBattlefieldAndReturn(player1, new RisingPopulace());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, player1, "Grizzly Bears");

        assertThat(populace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a planeswalker you control dies")
    void getsCounterWhenAllyPlaneswalkerDies() {
        Permanent populace = harness.addToBattlefieldAndReturn(player1, new RisingPopulace());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 0);
        ajani.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(populace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature dies")
    void doesNotTriggerWhenOpponentsCreatureDies() {
        Permanent populace = harness.addToBattlefieldAndReturn(player1, new RisingPopulace());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(populace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void killWithShock(Player caster, Player targetPlayer,
                               String targetName) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, harness.getPermanentId(targetPlayer, targetName));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
