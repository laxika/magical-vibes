package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoordinatedAssault;
import com.github.laxika.magicalvibes.cards.d.DragonMantle;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({WingsteedRider.class, Shock.class, GiantGrowth.class, CoordinatedAssault.class, DragonMantle.class})
class WingsteedRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Wingsteed Rider puts a +1/+1 counter on it")
    void castingSpellThatTargetsRiderPutsCounterOnIt() {
        harness.addToBattlefield(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID riderId = harness.getPermanentId(player1, "Wingsteed Rider");
        harness.castAndResolveInstant(player1, 0, riderId);
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Wingsteed Rider");
        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Wingsteed Rider")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent rider = findPermanent(player1, "Wingsteed Rider");
        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Wingsteed Rider does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WingsteedRider());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID riderId = harness.getPermanentId(player1, "Wingsteed Rider");
        harness.castAndResolveInstant(player2, 0, riderId);

        Permanent rider = findPermanent(player1, "Wingsteed Rider");
        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only the targeted Rider receives a heroic counter")
    void targetingAnotherRiderDoesNotTriggerThisRider() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new WingsteedRider());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targeted.getId());
        harness.passBothPriorities();

        assertThat(targeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each spell targeting the Rider adds another counter")
    void repeatedTargetedCastsAccumulateCounters() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new CoordinatedAssault(), new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, rider.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, rider.getId());
        harness.passBothPriorities();

        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell with two creature targets triggers each targeted Rider once")
    void multiTargetSpellTriggersBothRiders() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WingsteedRider());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Aura spell triggers heroic before the Aura resolves")
    void auraSpellTriggersHeroic() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new WingsteedRider());
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, rider.getId());
        harness.passBothPriorities();

        assertThat(rider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Dragon Mantle");
    }
}
