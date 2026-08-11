package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcyBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Taps X target creatures")
    void tapsXTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isZero();
        assertThat(second.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Ferocious keeps the targeted creatures tapped through their next untap step")
    void ferociousLocksTargetedCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not lock targets without ferocious")
    void doesNotLockWithoutFerocious() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
