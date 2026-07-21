package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GloryscaleViashinoTest extends BaseCardTest {

    private Permanent addViashino() {
        harness.addToBattlefield(player1, new GloryscaleViashino());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gets +3/+3 when you cast a multicolored spell")
    void pumpsWhenMulticoloredSpellCast() {
        Permanent viashino = addViashino();

        // A second Gloryscale Viashino ({1}{R}{G}{W}) is a multicolored spell.
        harness.setHand(player1, List.of(new GloryscaleViashino()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        // Cast trigger sits on the stack above the creature spell.
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(viashino.getPowerModifier()).isEqualTo(3);
        assertThat(viashino.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not pump when you cast a monocolored spell")
    void noPumpForMonocoloredSpell() {
        Permanent viashino = addViashino();

        // Grizzly Bears is a monocolored (green) spell.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // No cast trigger — only the creature spell is on the stack.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(viashino.getPowerModifier()).isEqualTo(0);
        assertThat(viashino.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent viashino = addViashino();

        harness.setHand(player1, List.of(new GloryscaleViashino()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the cast trigger (pump)

        assertThat(viashino.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(viashino.getPowerModifier()).isEqualTo(0);
        assertThat(viashino.getToughnessModifier()).isEqualTo(0);
    }
}
