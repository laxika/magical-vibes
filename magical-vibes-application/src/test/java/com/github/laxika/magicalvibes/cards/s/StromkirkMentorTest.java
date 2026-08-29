package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OliviasBloodsworn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StromkirkMentorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Vampire you control")
    void etbPutsCounterOnAnotherVampire() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new OliviasBloodsworn());
        castMentor(player1, vampire.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a non-Vampire you control")
    void etbCannotTargetNonVampire() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StromkirkMentor()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Vampire you control");
    }

    @Test
    @DisplayName("ETB cannot target an opponent's Vampire")
    void etbCannotTargetOpponentsVampire() {
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new OliviasBloodsworn());
        harness.setHand(player1, List.of(new StromkirkMentor()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentVampire.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another Vampire you control");
    }

    @Test
    @DisplayName("ETB can be cast without a target when no other Vampire is controlled")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new StromkirkMentor()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private void castMentor(Player player, UUID targetId) {
        harness.setHand(player, List.of(new StromkirkMentor()));
        harness.addMana(player, ManaColor.BLACK, 4);
        harness.castCreature(player, 0, 0, targetId);
    }
}
