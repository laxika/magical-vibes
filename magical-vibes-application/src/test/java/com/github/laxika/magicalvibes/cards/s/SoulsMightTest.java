package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulsMightTest extends BaseCardTest {

    @Test
    @DisplayName("Puts +1/+1 counters equal to a 2/2's power")
    void putsCountersEqualToPowerOfTwoTwo() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsMight()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("X scales with the target's power")
    void countersScaleWithPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsMight()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, giantId);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarksteelAxe());
        harness.setHand(player1, List.of(new SoulsMight()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID axeId = harness.getPermanentId(player1, "Darksteel Axe");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, axeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
