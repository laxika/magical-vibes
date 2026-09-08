package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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

@CardUsed({AsmiraHolyAvenger.class, BayFalcon.class, RayOfCommand.class, DarkBanishing.class})
class AsmiraHolyAvengerTest extends BaseCardTest {

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private Permanent addAsmira() {
        Permanent asmira = new Permanent(new AsmiraHolyAvenger());
        gd.playerBattlefields.get(player1.getId()).add(asmira);
        return asmira;
    }

    @Test
    @DisplayName("Gains a +1/+1 counter at end step for each of your creatures that died this turn")
    void gainsCountersForOwnDeaths() {
        Permanent asmira = addAsmira();
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 2, Integer::sum);

        advanceToEndStepAndResolve(player1);

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores creatures that died under an opponent's control")
    void ignoresOpponentDeaths() {
        Permanent asmira = addAsmira();
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player2.getId(), 3, Integer::sum);

        advanceToEndStepAndResolve(player1);

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers at each end step, including an opponent's turn")
    void triggersOnOpponentEndStep() {
        Permanent asmira = addAsmira();
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);

        advanceToEndStepAndResolve(player2);

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains no counter at end step when no creature of yours died")
    void noCounterWithoutDeaths() {
        Permanent asmira = addAsmira();

        advanceToEndStepAndResolve(player1);

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not count an opponent-owned creature that you controlled when it died")
    void doesNotCountOpponentOwnedCreatureThatYouControlledWhenItDied() {
        Permanent asmira = addAsmira();
        Permanent falcon = addCreatureReady(player2, new BayFalcon());

        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, falcon.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, falcon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertInGraveyard(player2, "Bay Falcon");

        advanceToEndStepAndResolve(player1);

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
