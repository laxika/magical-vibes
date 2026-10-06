package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OutlawMedic;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SheriffOfSafePassage.class, OutlawMedic.class, Plains.class})
class SheriffOfSafePassageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter when you control no other creatures")
    void entersWithOneCounter() {
        castSheriff();

        Permanent sheriff = findPermanent(player1, "Sheriff of Safe Passage");
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sheriff.getEffectivePower()).isEqualTo(1);
        assertThat(sheriff.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with an additional counter for each other creature you control")
    void countsOtherControlledCreatures() {
        harness.addToBattlefield(player1, new OutlawMedic());
        harness.addToBattlefield(player1, new OutlawMedic());

        castSheriff();

        Permanent sheriff = findPermanent(player1, "Sheriff of Safe Passage");
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(sheriff.getEffectivePower()).isEqualTo(3);
        assertThat(sheriff.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count creatures controlled by an opponent")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player1, new OutlawMedic());
        harness.addToBattlefield(player2, new OutlawMedic());
        harness.addToBattlefield(player2, new OutlawMedic());

        castSheriff();

        Permanent sheriff = findPermanent(player1, "Sheriff of Safe Passage");
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void ignoresNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new OutlawMedic());

        castSheriff();

        assertThat(findPermanent(player1, "Sheriff of Safe Passage")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureCountIsDeterminedWhenSheriffEnters() {
        harness.castFromHand(player1, new SheriffOfSafePassage(), "{2}{W}");
        harness.addToBattlefield(player1, new OutlawMedic());
        harness.passBothPriorities();

        Permanent sheriff = findPermanent(player1, "Sheriff of Safe Passage");
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.addToBattlefield(player1, new OutlawMedic());
        harness.runStateBasedActions();
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void plottedSheriffCastsForFreeOnLaterTurnAndCountsCurrentCreatures() {
        SheriffOfSafePassage sheriff = plotSheriff();

        harness.assertNotInHand(player1, "Sheriff of Safe Passage");
        harness.assertNotOnBattlefield(player1, "Sheriff of Safe Passage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, sheriff.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, sheriff.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new OutlawMedic());
        harness.addToBattlefield(player1, new OutlawMedic());
        harness.castFromExile(player1, sheriff.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sheriff of Safe Passage")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPlotOutsideMainPhase() {
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SheriffOfSafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Sheriff of Safe Passage");
    }

    @Test
    void cannotPlotWithSpellOnStack() {
        harness.castFromHand(player1, new OutlawMedic(), "{1}{W}");
        harness.setHand(player1, List.of(new SheriffOfSafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Sheriff of Safe Passage");
        assertThat(gd.stack).hasSize(1);
    }

    private SheriffOfSafePassage plotSheriff() {
        SheriffOfSafePassage sheriff = new SheriffOfSafePassage();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(sheriff));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        return sheriff;
    }

    private void castSheriff() {
        harness.castFromHand(player1, new SheriffOfSafePassage(), "{2}{W}");
        harness.passBothPriorities();
    }
}
