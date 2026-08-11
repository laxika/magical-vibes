package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RimeChillTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each distinct color among permanents you control")
    void reducesCostByDistinctControlledColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new RimeChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Taps and stuns up to two creatures, then draws a card")
    void tapsStunsAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RimeChill()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.assertInHand(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Rime Chill");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RimeChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
