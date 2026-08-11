package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiverwheelAerialistsTest extends BaseCardTest {

    private Permanent addAerialists() {
        harness.addToBattlefield(player1, new RiverwheelAerialists());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent aerialists = addAerialists();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aerialists)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aerialists)).isEqualTo(6);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent aerialists = addAerialists();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, aerialists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aerialists)).isEqualTo(5);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent aerialists = addAerialists();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aerialists)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aerialists)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aerialists)).isEqualTo(5);
    }
}
