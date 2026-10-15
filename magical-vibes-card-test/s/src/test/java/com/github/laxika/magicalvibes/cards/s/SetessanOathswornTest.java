package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MortalsResolve;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetessanOathsworn.class, GiantGrowth.class, Shock.class, MortalsResolve.class, SearingBlood.class})
class SetessanOathswornTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Setessan Oathsworn puts two +1/+1 counters on it")
    void castingSpellThatTargetsOathswornTriggersHeroic() {
        harness.addToBattlefield(player1, new SetessanOathsworn());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID oathswornId = harness.getPermanentId(player1, "Setessan Oathsworn");
        harness.castInstant(player1, 0, oathswornId);
        resolveAllTriggers();

        Permanent oathsworn = findPermanent(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Setessan Oathsworn")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanOathsworn());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent oathsworn = findPermanent(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Setessan Oathsworn does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanOathsworn());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID oathswornId = harness.getPermanentId(player1, "Setessan Oathsworn");
        harness.castAndResolveInstant(player2, 0, oathswornId);

        Permanent oathsworn = findPermanent(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heroic counters resolve before the targeting spell deals lethal damage")
    void countersResolveBeforeTargetingDamageSpell() {
        Permanent oathsworn = harness.addToBattlefieldAndReturn(player1, new SetessanOathsworn());
        harness.setHand(player1, List.of(new SearingBlood()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, oathsworn.getId());
        harness.passBothPriorities();

        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each targeting cast triggers only the targeted Oathsworn")
    void repeatedCastsOnlyPutCountersOnTargetedPermanent() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new SetessanOathsworn());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SetessanOathsworn());
        harness.setHand(player1, List.of(new MortalsResolve(), new MortalsResolve()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, targeted.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, targeted.getId());
        resolveAllTriggers();

        assertThat(targeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
