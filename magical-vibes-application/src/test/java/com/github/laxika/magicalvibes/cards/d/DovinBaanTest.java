package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DovinBaanTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives a creature -3/-0 and locks its activated abilities")
    void plusOneShrinksAndLocksCreature() {
        Permanent dovin = addReadyDovin(player1, 5);
        Permanent elves = addReadyPermanent(player2, new LlanowarElves());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(elves.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("+1 can resolve without a target")
    void plusOneAllowsNoTarget() {
        Permanent dovin = addReadyDovin(player1, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-1 gains 2 life and draws a card")
    void minusOneGainsLifeAndDraws() {
        Permanent dovin = addReadyDovin(player1, 5);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-7 limits only opponents to two untaps")
    void minusSevenLimitsOpponentsUntaps() {
        Permanent dovin = addReadyDovin(player1, 7);
        List<Permanent> ownPermanents = addTappedPermanents(player1, 3);
        List<Permanent> opposingPermanents = addTappedPermanents(player2, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(opposingPermanents.get(0).getId(), opposingPermanents.get(1).getId()));

        assertThat(opposingPermanents.get(0).isTapped()).isFalse();
        assertThat(opposingPermanents.get(1).isTapped()).isFalse();
        assertThat(opposingPermanents.get(2).isTapped()).isTrue();
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isZero();

        advanceToNextTurn(player2);

        assertThat(ownPermanents).allMatch(permanent -> !permanent.isTapped());
    }

    private Permanent addReadyDovin(Player player, int loyalty) {
        Permanent perm = new Permanent(new DovinBaan());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Permanent> addTappedPermanents(Player player, int count) {
        List<Permanent> permanents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent permanent = addReadyPermanent(player, new GrizzlyBears());
            permanent.tap();
            permanents.add(permanent);
        }
        return permanents;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
