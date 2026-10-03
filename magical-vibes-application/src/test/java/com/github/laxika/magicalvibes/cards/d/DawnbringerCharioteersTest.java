package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DawnbringerCharioteers.class, GiantGrowth.class, Shock.class})
class DawnbringerCharioteersTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Dawnbringer Charioteers puts a +1/+1 counter on it")
    void castingSpellThatTargetsCharioteersPutsCounterOnIt() {
        harness.addToBattlefield(player1, new DawnbringerCharioteers());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID charioteersId = harness.getPermanentId(player1, "Dawnbringer Charioteers");
        harness.castAndResolveInstant(player1, 0, charioteersId);
        harness.passBothPriorities();

        Permanent charioteers = findPermanent(player1, "Dawnbringer Charioteers");
        assertThat(charioteers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Dawnbringer Charioteers")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new DawnbringerCharioteers());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent charioteers = findPermanent(player1, "Dawnbringer Charioteers");
        assertThat(charioteers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Dawnbringer Charioteers does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new DawnbringerCharioteers());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID charioteersId = harness.getPermanentId(player1, "Dawnbringer Charioteers");
        harness.castAndResolveInstant(player2, 0, charioteersId);

        Permanent charioteers = findPermanent(player1, "Dawnbringer Charioteers");
        assertThat(charioteers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heroic resolves before the targeting spell and triggers again for each cast")
    void heroicResolvesBeforeSpellAndAccumulatesCounters() {
        harness.addToBattlefield(player1, new DawnbringerCharioteers());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID charioteersId = harness.getPermanentId(player1, "Dawnbringer Charioteers");
        Permanent charioteers = findPermanent(player1, "Dawnbringer Charioteers");

        harness.castInstant(player1, 0, charioteersId);
        assertThat(charioteers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(charioteers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, charioteersId);
        harness.passBothPriorities();
        assertThat(charioteers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dawnbringer Charioteers");
    }

    @Test
    @DisplayName("Only the targeted Dawnbringer Charioteers triggers heroic")
    void targetingAnotherCharioteersDoesNotTriggerThisOne() {
        harness.addToBattlefield(player1, new DawnbringerCharioteers());
        Permanent first = findPermanent(player1, "Dawnbringer Charioteers");
        harness.addToBattlefield(player1, new DawnbringerCharioteers());
        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, second.getId());
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
    }
}
