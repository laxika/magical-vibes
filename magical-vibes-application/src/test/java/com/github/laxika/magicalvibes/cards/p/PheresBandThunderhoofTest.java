package com.github.laxika.magicalvibes.cards.p;

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

class PheresBandThunderhoofTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Pheres-Band Thunderhoof puts two +1/+1 counters on it")
    void castingSpellThatTargetsThunderhoofTriggersHeroic() {
        harness.addToBattlefield(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID thunderhoofId = harness.getPermanentId(player1, "Pheres-Band Thunderhoof");
        harness.castInstant(player1, 0, thunderhoofId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Pheres-Band Thunderhoof's Heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Pheres-Band Thunderhoof does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PheresBandThunderhoof());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        UUID thunderhoofId = harness.getPermanentId(player1, "Pheres-Band Thunderhoof");
        harness.castInstant(player2, 0, thunderhoofId);
        harness.passBothPriorities();

        Permanent thunderhoof = findPermanent(player1, "Pheres-Band Thunderhoof");
        assertThat(thunderhoof.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
