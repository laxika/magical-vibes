package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FourKnocks;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KateStewart.class, FourKnocks.class, GrizzlyBears.class})
class KateStewartTest extends BaseCardTest {

    @Test
    @DisplayName("Putting time counters on a controlled permanent creates one Soldier")
    void timeCountersCreateOneSoldierPerEvent() {
        harness.addToBattlefield(player1, new KateStewart());
        harness.enterBattlefieldAndReturn(player1, new FourKnocks());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Time counters on an opponent's permanent do not trigger Kate")
    void opponentPermanentDoesNotTriggerKate() {
        harness.addToBattlefield(player1, new KateStewart());
        harness.enterBattlefieldAndReturn(player2, new FourKnocks());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Paying eight mana boosts only attacking creatures by controlled time counters")
    void payingOnAttackBoostsAttackersByTimeCounterCount() {
        addCreatureReady(player1, new KateStewart());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent timeCounterPermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        timeCounterPermanent.setCounterCount(CounterType.TIME, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
            assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, nonattacker)).isEqualTo(2);
        });
    }
}
