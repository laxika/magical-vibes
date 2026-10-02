package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AntManScottLang.class})
class AntManScottLangTest extends BaseCardTest {

    @Test
    void paysFourManaToPutPlusOnePlusOneCounterOnItself() {
        Permanent antMan = addCreatureReady(player1, new AntManScottLang());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent antMan = harness.addToBattlefieldAndReturn(player1, new AntManScottLang());
        antMan.setSummoningSick(true);
        antMan.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(antMan.isTapped()).isTrue();
    }

    @Test
    void canActivateTwiceBeforeEitherActivationResolves() {
        Permanent antMan = addCreatureReady(player1, new AntManScottLang());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotPutCounterOnNewObjectAfterSourceLeavesBattlefield() {
        AntManScottLang card = new AntManScottLang();
        Permanent original = addCreatureReady(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
