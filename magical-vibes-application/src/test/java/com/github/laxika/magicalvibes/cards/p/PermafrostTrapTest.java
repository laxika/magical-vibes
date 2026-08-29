package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermafrostTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Taps up to two target creatures and locks their next untap step")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast for {U} after an opponent green creature entered this turn")
    void castsForAlternateCostAfterOpponentGreenCreatureEntered() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(creature.getCard()));
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Permafrost Trap");
    }

    @Test
    @DisplayName("Alternate cost requires an opponent green creature to have entered this turn")
    void alternateCostRequiresOpponentGreenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(creature.getCard()));
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PermafrostTrap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
