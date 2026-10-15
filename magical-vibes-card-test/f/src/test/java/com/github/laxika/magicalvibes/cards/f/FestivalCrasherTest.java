package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FestivalCrasher.class, Shock.class, GrizzlyBears.class, LavaAxe.class})
class FestivalCrasherTest extends BaseCardTest {

    private Permanent addCrasher() {
        Permanent crasher = harness.addToBattlefieldAndReturn(player1, new FestivalCrasher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return crasher;
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

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

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

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(crasher.getPowerModifier()).isEqualTo(0);
        assertThat(crasher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sorcery cast boosts before the spell resolves")
    void pumpsWhenSorceryCast() {
        Permanent crasher = addCrasher();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(crasher.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(crasher.getPowerModifier()).isEqualTo(2);
        assertThat(crasher.getToughnessModifier()).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Opponent's instant does not boost Festival Crasher")
    void opponentsInstantDoesNotPump() {
        Permanent crasher = addCrasher();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(crasher.getPowerModifier()).isZero();
        assertThat(crasher.getToughnessModifier()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each Festival Crasher boosts itself independently")
    void multipleCrashersEachPump() {
        Permanent first = addCrasher();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FestivalCrasher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        harness.assertLife(player2, 20);
    }
}
