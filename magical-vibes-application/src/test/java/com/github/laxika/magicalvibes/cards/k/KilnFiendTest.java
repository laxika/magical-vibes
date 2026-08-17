package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FlameSlash;
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

class KilnFiendTest extends BaseCardTest {

    private Permanent addFiend() {
        harness.addToBattlefield(player1, new KilnFiend());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Kiln Fiend");
    }

    @Test
    @DisplayName("Gets +3/+0 when you cast an instant")
    void pumpsWhenInstantCast() {
        Permanent fiend = addFiend();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.castSorcery(player1, 0, findPermanent(player2, "Grizzly Bears").getId());
        harness.passBothPriorities();

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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fiend.getPowerModifier()).isEqualTo(0);
        assertThat(fiend.getToughnessModifier()).isEqualTo(0);
    }
}
