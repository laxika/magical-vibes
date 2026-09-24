package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.cards.t.TheOzolith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoshimaruEverFaithful.class, TheOzolith.class, GrizzlyBears.class,
        TymaretTheMurderKing.class})
class YoshimaruEverFaithfulTest extends BaseCardTest {

    @Test
    void doesNotTriggerFromItsOwnEntry() {
        harness.setHand(player1, List.of(new YoshimaruEverFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Yoshimaru, Ever Faithful")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterOnEntryOfLegendaryNoncreaturePermanentYouControl() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());

        harness.enterBattlefieldAndReturn(player1, new TheOzolith());
        harness.passBothPriorities();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNonlegendaryPermanent() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForLegendaryPermanentAnOpponentControls() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TymaretTheMurderKing(), "{1}{B}{R}");
        harness.passBothPriorities();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
