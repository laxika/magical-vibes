package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchMawNephilim.class, Quicken.class})
class WitchMawNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("May put two +1/+1 counters on itself when its controller casts a spell")
    void mayPutTwoCountersWhenControllerCastsSpell() {
        harness.addToBattlefield(player1, new WitchMawNephilim());
        harness.castFromHand(player1, new WitchMawNephilim(), "{G}{W}{U}{B}");

        Permanent nephilim = findPermanent(player1, "Witch-Maw Nephilim");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(nephilim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the spell-cast may ability does not add counters")
    void decliningMayDoesNotAddCounters() {
        harness.addToBattlefield(player1, new WitchMawNephilim());
        harness.castFromHand(player1, new WitchMawNephilim(), "{G}{W}{U}{B}");

        Permanent nephilim = findPermanent(player1, "Witch-Maw Nephilim");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(nephilim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell")
    void doesNotTriggerWhenOpponentCastsSpell() {
        Permanent nephilim = addReadyNephilim();
        harness.castFromHand(player2, new Quicken(), "{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
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
    @DisplayName("Checks power when the attack trigger resolves")
    void checksPowerWhenAttackTriggerResolves() {
        Permanent nephilim = addReadyNephilim();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);

        declareAttackers(player1, List.of(0));
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
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

    @Test
    @DisplayName("Does not gain trample if power drops below ten before resolution")
    void powerDropsBeforeAttackTriggerResolves() {
        Permanent nephilim = addReadyNephilim();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);

        declareAttackers(player1, List.of(0));
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nephilim, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample persists if power drops after the attack trigger resolves")
    void tramplePersistsAfterPowerDrops() {
        Permanent nephilim = addReadyNephilim();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        nephilim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);

        assertThat(gqs.hasKeyword(gd, nephilim, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Only the existing Nephilim gains counters when another Nephilim is cast")
    void newlyCastNephilimDoesNotTriggerForItself() {
        Permanent nephilim = addReadyNephilim();
        harness.castFromHand(player1, new WitchMawNephilim(), "{G}{W}{U}{B}");

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(nephilim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent newlyEntered = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(newlyEntered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyNephilim() {
        return addCreatureReady(player1, new WitchMawNephilim());
    }
}
