package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BreachingHippocampTest extends BaseCardTest {

    @Test
    @DisplayName("ETB untaps another creature you control")
    void etbUntapsAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        bears.tap();

        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        UUID targetId = bears.getId();
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Rejects an opponent's creature as target")
    void rejectsOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Rejects a non-creature as target")
    void rejectsNonCreature() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Can be cast without a target when no other creatures are controlled")
    void canBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new BreachingHippocamp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
