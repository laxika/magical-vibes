package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SanguineGlorifierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Vampire you control")
    void etbPutsCounterOnAnotherVampireYouControl() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID vampireId = harness.getPermanentId(player1, "Vampire Interloper");
        gs.playCard(gd, player1, 0, 0, vampireId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(vampireId))
                .findFirst().orElseThrow();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target the entering Vampire")
    void etbCannotTargetEnteringVampire() {
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent glorifier = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(glorifier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rejects a non-Vampire as target")
    void rejectsNonVampireTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vampire you control");
    }

    @Test
    @DisplayName("Rejects an opponent's Vampire as target")
    void rejectsOpponentsVampire() {
        harness.addToBattlefield(player2, new VampireInterloper());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID vampireId = harness.getPermanentId(player2, "Vampire Interloper");
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, vampireId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vampire you control");
    }

    @Test
    @DisplayName("Resolving the creature puts its ETB trigger on the stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new SanguineGlorifier()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID vampireId = harness.getPermanentId(player1, "Vampire Interloper");
        gs.playCard(gd, player1, 0, 0, vampireId, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(vampireId);
    }
}
