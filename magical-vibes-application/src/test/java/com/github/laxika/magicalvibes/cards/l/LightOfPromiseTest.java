package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightOfPromise.class, AngelOfMercy.class, GrizzlyBears.class,
        Revitalize.class, ReturnToNature.class})
class LightOfPromiseTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets counters equal to life gained")
    void putsCountersEqualToLifeGained() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Angel of Mercy
        harness.passBothPriorities(); // resolve its life-gain trigger
        harness.passBothPriorities(); // resolve the granted trigger

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void usesEnchantedCreaturesControllerRatherThanAuraController() {
        Permanent host = addCreatureReady(player2, new GrizzlyBears());
        enchantHost(host);

        gainThreeLife(player1);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        gainThreeLife(player2);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void triggersForEveryLifeGainAndOnlyPutsCountersOnHost() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);

        gainThreeLife(player1);
        gainThreeLife(player1);

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleAurasGrantSeparateTriggers() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);
        enchantHost(host);

        gainThreeLife(player1);
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void removingAuraDoesNotStopPendingTriggerButStopsFutureTriggers() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        enchantHost(host);
        Permanent aura = findPermanent(player1, "Light of Promise");
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 1, aura.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Light of Promise");
        harness.passBothPriorities();
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        gainThreeLife(player1);
        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void gainThreeLife(Player player) {
        harness.setLibrary(player, List.of(new GrizzlyBears()));
        harness.setHand(player, List.of(new Revitalize()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player, 0);
        harness.passBothPriorities();
    }

    private void enchantHost(Permanent host) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LightOfPromise()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
    }
}
