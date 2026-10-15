package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimPoppet.class, SafeholdSentry.class})
class GrimPoppetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters (4/4 becomes 1/1)")
    void entersWithThreeMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrimPoppet()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent poppet = findPoppet(player1);

        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(poppet.getEffectivePower()).isEqualTo(1);
        assertThat(poppet.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability removes a -1/-1 counter from itself and puts one on target creature")
    void abilityMovesCounterToTarget() {
        Permanent poppet = addReadyPoppet(player1);
        harness.addToBattlefield(player2, new SafeholdSentry());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Cost: removed one -1/-1 counter from Grim Poppet (3 -> 2)
        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        // Effect: target gained a -1/-1 counter
        Permanent bears = harness.getGameQueryService().findPermanentById(gd, bearsId);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two -1/-1 counters kill a 2/2 target creature")
    void twoCountersKillTarget() {
        addReadyPoppet(player1);
        harness.addToBattlefield(player2, new SafeholdSentry());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    @Test
    @DisplayName("Cannot activate ability when no -1/-1 counters remain")
    void cannotActivateWithoutCounters() {
        Permanent poppet = addReadyPoppet(player1);
        poppet.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.addToBattlefield(player2, new SafeholdSentry());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearsId = harness.getPermanentId(player2, "Safehold Sentry");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot target itself (another target creature)")
    void cannotTargetSelf() {
        Permanent poppet = addReadyPoppet(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, poppet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter removal is paid immediately even while tapped and summoning sick")
    void removesCounterBeforeResolutionWithoutTapRestriction() {
        Permanent poppet = addReadyPoppet(player1);
        poppet.tap();
        poppet.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(poppet.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Grim Poppet is a legal target")
    void canTargetAnotherGrimPoppet() {
        Permanent poppet = addReadyPoppet(player1);
        Permanent otherPoppet = addReadyPoppet(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, otherPoppet.getId());
        harness.passBothPriorities();

        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grim Poppet");
        harness.assertInGraveyard(player2, "Grim Poppet");
    }

    @Test
    @DisplayName("Ability still resolves when its source has left the battlefield")
    void resolvesWithoutSource() {
        Permanent poppet = addReadyPoppet(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(poppet);
        gd.playerGraveyards.get(player1.getId()).add(poppet.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target does not refund the counter removal cost")
    void targetLeavingDoesNotRefundCost() {
        Permanent poppet = addReadyPoppet(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(poppet.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPoppet(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrimPoppet());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        return perm;
    }

    private Permanent findPoppet(Player player) {
        return findPermanent(player, "Grim Poppet");
    }
}
