package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell gives +X/+X equal to that spell's mana value")
    void castingSpellBoostsByManaValue() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new GrizzlyBears())); // mana value 2
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent manaplasm = getManaplasm();
        assertThat(manaplasm.getPowerModifier()).isEqualTo(2);
        assertThat(manaplasm.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost scales with the cast spell's mana value")
    void boostScalesWithManaValue() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new JacesIngenuity())); // mana value 5
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent manaplasm = getManaplasm();
        assertThat(manaplasm.getPowerModifier()).isEqualTo(5);
        assertThat(manaplasm.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Manaplasm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent manaplasm = getManaplasm();
        assertThat(manaplasm.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(manaplasm.getPowerModifier()).isEqualTo(0);
        assertThat(manaplasm.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent getManaplasm() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Manaplasm"))
                .findFirst()
                .orElseThrow();
    }
}
