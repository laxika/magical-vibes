package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FestivalCrasherTest extends BaseCardTest {

    private Permanent addCrasher() {
        harness.addToBattlefield(player1, new FestivalCrasher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gets +2/+0 when you cast an instant")
    void pumpsWhenInstantCast() {
        Permanent crasher = addCrasher();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Cast trigger sits on the stack above Shock.
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(crasher.getPowerModifier()).isEqualTo(2);
        assertThat(crasher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not pump when you cast a creature spell")
    void noPumpForCreatureSpell() {
        Permanent crasher = addCrasher();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(crasher.getPowerModifier()).isEqualTo(0);
        assertThat(crasher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boosts stack across multiple instant casts")
    void pumpsStack() {
        Permanent crasher = addCrasher();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // pump
        harness.passBothPriorities(); // Shock

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // pump

        assertThat(crasher.getPowerModifier()).isEqualTo(4);
        assertThat(crasher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent crasher = addCrasher();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(crasher.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crasher.getPowerModifier()).isEqualTo(0);
        assertThat(crasher.getToughnessModifier()).isEqualTo(0);
    }
}
