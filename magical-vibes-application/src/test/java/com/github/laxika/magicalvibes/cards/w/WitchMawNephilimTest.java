package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WitchMawNephilim.class, GrizzlyBears.class})
class WitchMawNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("May put two +1/+1 counters on itself when its controller casts a spell")
    void mayPutTwoCountersWhenControllerCastsSpell() {
        harness.addToBattlefield(player1, new WitchMawNephilim());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent nephilim = findPermanent(player1, "Witch-Maw Nephilim");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(nephilim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the spell-cast may ability does not add counters")
    void decliningMayDoesNotAddCounters() {
        harness.addToBattlefield(player1, new WitchMawNephilim());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent nephilim = findPermanent(player1, "Witch-Maw Nephilim");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(nephilim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gains trample when it attacks with power 10 or greater")
    void gainsTrampleAtPowerTen() {
        Permanent nephilim = addReadyNephilim();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nephilim, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain trample when it attacks with power less than 10")
    void doesNotGainTrampleBelowPowerTen() {
        Permanent nephilim = addReadyNephilim();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nephilim, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger's trample grant wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent nephilim = addReadyNephilim();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, nephilim, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nephilim, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyNephilim() {
        return addCreatureReady(player1, new WitchMawNephilim());
    }
}
