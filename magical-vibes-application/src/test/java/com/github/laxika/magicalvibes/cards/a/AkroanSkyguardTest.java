package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AkroanSkyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Akroan Skyguard puts a +1/+1 counter on it")
    void castingSpellThatTargetsSkyguardPutsCounterOnIt() {
        harness.addToBattlefield(player1, new AkroanSkyguard());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID skyguardId = harness.getPermanentId(player1, "Akroan Skyguard");
        harness.castInstant(player1, 0, skyguardId);
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, skyguardId);
        harness.passBothPriorities();

        Permanent skyguard = findPermanent(player1, "Akroan Skyguard");
        assertThat(skyguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
