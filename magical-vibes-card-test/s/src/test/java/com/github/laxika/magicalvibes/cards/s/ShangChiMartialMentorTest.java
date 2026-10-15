package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShangChiMartialMentor.class, GrizzlyBears.class, TimberlandGuide.class})
class ShangChiMartialMentorTest extends BaseCardTest {

    @Test
    void doublesPlusOnePlusOneCountersPutOnYourCreatures() {
        harness.addToBattlefield(player1, new ShangChiMartialMentor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void powerUpPutsThreeCountersWhichAreDoubled() {
        Permanent shangChi = harness.enterBattlefieldAndReturn(player1, new ShangChiMartialMentor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void doesNotDoubleCountersOnOpponentsCreatures() {
        harness.addToBattlefield(player1, new ShangChiMartialMentor());
        Permanent guide = harness.addToBattlefieldAndReturn(player2, new TimberlandGuide());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, guide.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doublesCountersEvenWhenTheEffectIsControlledByAnOpponent() {
        harness.addToBattlefield(player1, new ShangChiMartialMentor());
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new TimberlandGuide());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0, guide.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void powerUpRequiresFullCostWhenItDidNotEnterThisTurn() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiMartialMentor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void powerUpCannotBeActivatedAgainWhileFirstActivationIsOnTheStack() {
        Permanent shangChi = harness.enterBattlefieldAndReturn(player1, new ShangChiMartialMentor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }
}
