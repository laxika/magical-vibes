package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.t.Terror;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlingDervish.class, GrizzlyBears.class, HermeticStudy.class, Terror.class})
class WhirlingDervishTest extends BaseCardTest {

    private Permanent addDervishWithHermeticStudy() {
        Permanent dervish = addCreatureReady(player1, new WhirlingDervish());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(dervish.getId());
        return dervish;
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    void getsCounterAfterDealingNoncombatDamage() {
        Permanent dervish = addDervishWithHermeticStudy();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        advanceToEndStepAndResolve(player1);
        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    @Test
    @DisplayName("Gets a +1/+1 counter at end step after dealing damage to an opponent")
    void getsCounterAfterDealingDamage() {
        Permanent dervish = addCreatureReady(player1, new WhirlingDervish());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 19);

        advanceToEndStepAndResolve(player1);

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets no counter when it dealt no damage this turn")
    void noCounterWithoutDamage() {
        Permanent dervish = addCreatureReady(player1, new WhirlingDervish());

        advanceToEndStepAndResolve(player1);

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt only to its own controller does not qualify")
    void noCounterWhenDamageNotToOpponent() {
        Permanent dervish = addDervishWithHermeticStudy();

        // Damage recorded against its own controller (not an opponent) — must not trigger.
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        advanceToEndStepAndResolve(player1);

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's, when it dealt damage to an opponent")
    void triggersOnEachEndStep() {
        Permanent dervish = addCreatureReady(player1, new WhirlingDervish());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.assertLife(player2, 19);

        advanceToEndStepAndResolve(player2);

        assertThat(dervish.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    @Test
    void protectionFromBlackPreventsBlackSpellTargeting() {
        Permanent dervish = addCreatureReady(player2, new WhirlingDervish());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, dervish.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
