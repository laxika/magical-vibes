package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.s.SilkwingScout;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({VigeanGraftmage.class, SilkwingScout.class, AzoriusSignet.class})
class VigeanGraftmageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent graftmage = castGraftmage();

        assertThat(graftmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Graft moves a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent graftmage = castGraftmage();

        Permanent scout = castSilkwingScout(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(graftmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto an opponent's creature")
    void graftMovesCounterOntoOpponentCreature() {
        Permanent graftmage = castGraftmage();

        Permanent scout = castSilkwingScout(player2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(graftmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may decline to move a counter")
    void graftMayDeclineToMoveCounter() {
        Permanent graftmage = castGraftmage();

        Permanent scout = castSilkwingScout(player1);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(graftmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Graft does not trigger for a noncreature entering")
    void graftDoesNotTriggerForNoncreatureEntering() {
        Permanent graftmage = castGraftmage();

        Permanent signet = harness.enterBattlefieldAndReturn(player1, new AzoriusSignet());

        assertThat(graftmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(signet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Untaps a target creature with a +1/+1 counter")
    void untapsTargetCreatureWithCounter() {
        Permanent scout = addCreatureReady(player2, new SilkwingScout());
        scout.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        scout.tap();
        Permanent graftmage = castGraftmage();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(graftmage),
                null, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent scout = addCreatureReady(player2, new SilkwingScout());
        Permanent graftmage = castGraftmage();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(graftmage),
                null, scout.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature with a +1/+1 counter")
    void cannotTargetNoncreatureWithCounter() {
        Permanent signet = harness.enterBattlefieldAndReturn(player2, new AzoriusSignet());
        signet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent graftmage = castGraftmage();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(graftmage),
                null, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untap does not resolve if the target loses its last +1/+1 counter")
    void doesNotUntapTargetThatLosesLastCounter() {
        Permanent scout = addCreatureReady(player2, new SilkwingScout());
        scout.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        scout.tap();
        Permanent graftmage = castGraftmage();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(graftmage),
                null, scout.getId());
        scout.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Graftmage can untap itself")
    void canUntapItselfWhileTappedAndSummoningSick() {
        Permanent graftmage = castGraftmage();
        graftmage.tap();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(graftmage),
                null, graftmage.getId());
        harness.passBothPriorities();

        assertThat(graftmage.isTapped()).isFalse();
        assertThat(graftmage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moving the final graft counter puts the Graftmage into the graveyard")
    void movingFinalCounterKillsGraftmage() {
        Permanent graftmage = castGraftmage();
        graftmage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent scout = castSilkwingScout(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Vigean Graftmage");
        harness.assertInGraveyard(player1, "Vigean Graftmage");
    }

    private Permanent castGraftmage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new VigeanGraftmage(), "{2}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Vigean Graftmage");
    }

    private Permanent castSilkwingScout(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new SilkwingScout(), "{2}{U}");
        harness.passBothPriorities();
        return findPermanent(player, "Silkwing Scout");
    }
}
