package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KilnFiend.class, FlameSlash.class, GrizzlyBears.class, Shock.class})
class KilnFiendTest extends BaseCardTest {

    private Permanent addFiend() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new KilnFiend());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return fiend;
    }

    @Test
    @DisplayName("Gets +3/+0 when you cast an instant")
    void pumpsWhenInstantCast() {
        Permanent fiend = addFiend();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(fiend.getPowerModifier()).isEqualTo(3);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +3/+0 when you cast a sorcery")
    void pumpsWhenSorceryCast() {
        Permanent fiend = addFiend();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, findPermanent(player2, "Grizzly Bears").getId());

        assertThat(fiend.getPowerModifier()).isEqualTo(3);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger for creature spells")
    void doesNotPumpForCreatureSpell() {
        Permanent fiend = addFiend();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(0);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent fiend = addFiend();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(fiend.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(0);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's instant does not trigger the boost")
    void opponentInstantDoesNotPump() {
        Permanent fiend = addFiend();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(fiend.getPowerModifier()).isZero();
        assertThat(fiend.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each instant adds a separate boost before the spell resolves")
    void multipleCastsGiveCumulativeBoosts() {
        Permanent fiend = addFiend();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(fiend.getPowerModifier()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(fiend.getPowerModifier()).isEqualTo(6);
        assertThat(fiend.getToughnessModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("Removing the source in response prevents its pending boost from affecting another Fiend")
    void removedSourceDoesNotBoostAnotherFiend() {
        Permanent fiend = addFiend();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new KilnFiend());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        gs.passPriority(gd, player1);
        harness.castAndResolveInstant(player2, 0, fiend.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fiend);

        for (int rounds = 0; rounds < 3 && !gd.stack.isEmpty(); rounds++) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isEqualTo(3);
        assertThat(other.getToughnessModifier()).isZero();
    }
}
