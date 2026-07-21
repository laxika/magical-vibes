package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoubleNegativeTest extends BaseCardTest {

    // player1 puts a creature spell and an instant spell on the stack and keeps a Double Negative in
    // hand at index 0. Double Negative may legally target either spell (a spell's own controller is
    // allowed), which keeps this a single-player priority sequence.
    private void stackTwoSpells(GrizzlyBears bears, AngelsMercy mercy) {
        harness.setHand(player1, List.of(bears, mercy, new DoubleNegative()));
        // Colorless over-covers every generic cost; the colored pips are over-provisioned so Double
        // Negative's {U}{U}{R} is never starved by an earlier spell's generic being paid with blue/red.
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);  // Grizzly Bears {1}{G}
        harness.addMana(player1, ManaColor.WHITE, 3);  // Angel's Mercy {2}{W}
        harness.addMana(player1, ManaColor.BLUE, 4);   // Double Negative {U}{U}{R}
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0); // Grizzly Bears creature spell on the stack
        harness.castInstant(player1, 0);  // Angel's Mercy instant spell on the stack
    }

    @Test
    @DisplayName("Counters both target spells")
    void countersBothTargetSpells() {
        int initialLife = gd.playerLifeTotals.get(player1.getId());
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        stackTwoSpells(bears, mercy);

        harness.castInstant(player1, 0, List.of(bears.getId(), mercy.getId()));

        StackEntry dn = gd.stack.getLast();
        assertThat(dn.getCard().getName()).isEqualTo("Double Negative");
        assertThat(dn.getTargetIds()).containsExactlyInAnyOrder(bears.getId(), mercy.getId());

        harness.passBothPriorities(); // resolve Double Negative — counters both

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                || se.getCard().getName().equals("Angel's Mercy"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Angel's Mercy"));
        // Angel's Mercy was countered, so no life was gained.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife);
    }

    @Test
    @DisplayName("Counters only the chosen spell when fewer than two are targeted")
    void countersOnlyChosenSpell() {
        int initialLife = gd.playerLifeTotals.get(player1.getId());
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        stackTwoSpells(bears, mercy);

        // Target only the creature spell; the instant resolves for 7 life.
        harness.castInstant(player1, 0, List.of(bears.getId()));

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife + 7);
    }

    @Test
    @DisplayName("Cannot target the same spell twice")
    void cannotTargetSameSpellTwice() {
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        stackTwoSpells(bears, mercy);

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target something that is not a spell on the stack")
    void cannotTargetNonSpell() {
        GrizzlyBears onBattlefield = new GrizzlyBears();
        harness.addToBattlefield(player1, onBattlefield);

        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        stackTwoSpells(bears, mercy);

        UUID battlefieldPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mercy.getId(), battlefieldPermanentId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
