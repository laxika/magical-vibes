package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquastrandSpider.class, MistralCharger.class, AzoriusSignet.class})
class AquastrandSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent spider = harness.enterBattlefieldAndReturn(player1, new AquastrandSpider());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("May move a +1/+1 counter onto another creature that enters")
    void mayMoveCounterOntoEnteringCreature() {
        Permanent spider = addSpider(player1);
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Mistral Charger");
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May decline moving a +1/+1 counter onto another creature")
    void mayDeclineMovingCounter() {
        Permanent spider = addSpider(player1);
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent charger = findPermanent(player1, "Mistral Charger");
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May move a +1/+1 counter onto an opponent's creature that enters")
    void mayMoveCounterOntoOpponentsEnteringCreature() {
        Permanent spider = addSpider(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");

        harness.passBothPriorities();
        Permanent charger = findPermanent(player2, "Mistral Charger");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature enters")
    void doesNotTriggerForNoncreatureEntering() {
        Permanent spider = addSpider(player1);
        Permanent signet = harness.enterBattlefieldAndReturn(player1, new AzoriusSignet());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(signet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gives a target creature with a +1/+1 counter reach until end of turn")
    void grantsReachUntilEndOfTurn() {
        Permanent spider = addSpider(player1);
        Permanent target = addCreatureReady(player1, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
        assertThat(spider.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Can give an opponent's creature with a +1/+1 counter reach")
    void grantsReachToOpponentsCreature() {
        Permanent spider = addSpider(player1);
        Permanent target = addCreatureReady(player2, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        addSpider(player1);
        Permanent target = addCreatureReady(player1, new MistralCharger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with a +1/+1 counter")
    void cannotTargetNoncreatureWithCounter() {
        addSpider(player1);
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        signet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Reach ability does not resolve if the target loses its last +1/+1 counter")
    void doesNotGrantReachWhenTargetLosesCounterBeforeResolution() {
        addSpider(player1);
        Permanent target = addCreatureReady(player1, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Reach remains after the target loses its counter following resolution")
    void retainsReachWhenCounterIsRemovedAfterResolution() {
        addSpider(player1);
        Permanent target = addCreatureReady(player1, new MistralCharger());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Can target itself with reach while summoning sick")
    void grantsReachToItselfWhileSummoningSick() {
        Permanent spider = addSpider(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, spider.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, spider, Keyword.REACH)).isTrue();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(spider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Moving its last counter still gives the entering creature a counter before the Spider dies")
    void movesLastCounterAndThenDies() {
        Permanent spider = addSpider(player1);
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Mistral Charger");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Aquastrand Spider");
        harness.assertInGraveyard(player1, "Aquastrand Spider");
    }

    private Permanent addSpider(Player player) {
        return harness.enterBattlefieldAndReturn(player, new AquastrandSpider());
    }
}
