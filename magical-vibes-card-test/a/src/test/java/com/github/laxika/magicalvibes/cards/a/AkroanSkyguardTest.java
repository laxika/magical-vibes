package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MortalsResolve;
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

@CardUsed({AkroanSkyguard.class, GiantGrowth.class, Shock.class, MortalsResolve.class})
class AkroanSkyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Akroan Skyguard puts a +1/+1 counter on it")
    void castingSpellThatTargetsSkyguardPutsCounterOnIt() {
        harness.addToBattlefield(player1, new AkroanSkyguard());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID skyguardId = harness.getPermanentId(player1, "Akroan Skyguard");
        harness.castAndResolveInstant(player1, 0, skyguardId);
        harness.passBothPriorities();

        Permanent skyguard = findPermanent(player1, "Akroan Skyguard");
        assertThat(skyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Akroan Skyguard")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AkroanSkyguard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent skyguard = findPermanent(player1, "Akroan Skyguard");
        assertThat(skyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Akroan Skyguard does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AkroanSkyguard());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID skyguardId = harness.getPermanentId(player1, "Akroan Skyguard");
        harness.castAndResolveInstant(player2, 0, skyguardId);

        Permanent skyguard = findPermanent(player1, "Akroan Skyguard");
        assertThat(skyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heroic resolves before the targeting spell and triggers again for another cast")
    void heroicResolvesBeforeSpellAndTriggersForEachCast() {
        harness.addToBattlefield(player1, new AkroanSkyguard());
        harness.setHand(player1, List.of(new MortalsResolve(), new MortalsResolve()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        UUID skyguardId = harness.getPermanentId(player1, "Akroan Skyguard");

        harness.castInstant(player1, 0, skyguardId);
        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanent(player1, "Akroan Skyguard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanent(player1, "Akroan Skyguard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, skyguardId);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Akroan Skyguard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the targeted Skyguard triggers when its controller casts the spell")
    void targetingAnotherSkyguardDoesNotTriggerUntargetedSkyguard() {
        harness.addToBattlefield(player1, new AkroanSkyguard());
        UUID firstId = harness.getPermanentId(player1, "Akroan Skyguard");
        harness.addToBattlefield(player1, new AkroanSkyguard());
        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(firstId))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new MortalsResolve()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, second.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Akroan Skyguard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
