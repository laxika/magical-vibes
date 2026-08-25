package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadeSeedstones.class, JadeheartAttendant.class, GrizzlyBears.class})
class JadeSeedstonesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes three +1/+1 counters among creatures you control")
    void distributesCountersAmongControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 2);

        harness.setHand(player1, List.of(new JadeSeedstones()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Craft returns Jadeheart Attendant and gains life equal to the craft material's mana value")
    void craftsAndGainsLifeEqualToMaterialManaValue() {
        Permanent seedstones = harness.addToBattlefieldAndReturn(player1, new JadeSeedstones());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent attendant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof JadeheartAttendant)
                .findFirst().orElseThrow();
        assertThat(attendant.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
    }
}
