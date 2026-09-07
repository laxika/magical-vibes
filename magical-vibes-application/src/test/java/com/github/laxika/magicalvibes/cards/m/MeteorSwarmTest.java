package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeteorSwarm.class, ChandraNalaar.class, GrizzlyBears.class})
class MeteorSwarmTest extends BaseCardTest {

    @Test
    void dividesEightDamageAmongExactlyXCreaturesAndPlaneswalkers() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorceryForX(player1, 0, 2, Map.of(
                bears.getId(), 4,
                chandra.getId(), 4
        ));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void requiresExactlyXTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 2, Map.of(bears.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new MeteorSwarm()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorceryForX(
                player1, 0, 1, Map.of(player2.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }
}
