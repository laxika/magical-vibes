package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HollowhengeHuntmaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvabruckCaretaker.class, HollowhengeHuntmaster.class, GrizzlyBears.class})
class AvabruckCaretakerTest extends BaseCardTest {

    @Test
    void caretakerPutsTwoCountersOnAnotherCreatureYouControlAtCombat() {
        gd.dayNight = DayNight.DAY;
        Permanent caretaker = harness.enterBattlefieldAndReturn(player1, new AvabruckCaretaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(caretaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void caretakerCannotTargetItselfOrAnOpponentsCreature() {
        gd.dayNight = DayNight.DAY;
        Permanent caretaker = harness.enterBattlefieldAndReturn(player1, new AvabruckCaretaker());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, caretaker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void huntmasterGivesOtherPermanentsHexproofAndCountersEachCreature() {
        gd.dayNight = DayNight.NIGHT;
        Permanent huntmaster = harness.enterBattlefieldAndReturn(player1, new AvabruckCaretaker());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(huntmaster.getCard()).isInstanceOf(HollowhengeHuntmaster.class);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(huntmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent caretaker = harness.enterBattlefieldAndReturn(player1, new AvabruckCaretaker());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(caretaker.getCard()).isInstanceOf(HollowhengeHuntmaster.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(caretaker.getCard()).isInstanceOf(AvabruckCaretaker.class);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
