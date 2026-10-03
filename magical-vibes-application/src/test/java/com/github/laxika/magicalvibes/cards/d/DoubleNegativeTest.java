package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoubleNegative.class, GrizzlyBears.class, AngelsMercy.class, SpellbreakerBehemoth.class})
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
        harness.addMana(player1, ManaColor.WHITE, 3);  // Angel's Mercy {2}{W}{W}
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

        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                || se.getCard().getName().equals("Angel's Mercy"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Angel's Mercy");
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

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
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

    @Test
    @DisplayName("Can be cast with no targets on an empty stack")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new DoubleNegative()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Double Negative");
    }

    @Test
    @DisplayName("Counters the remaining target when another target has left the stack")
    void countersRemainingLegalTarget() {
        int initialLife = gd.playerLifeTotals.get(player1.getId());
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        stackTwoSpells(bears, mercy);
        harness.setHand(player1, List.of(new DoubleNegative(), new DoubleNegative()));

        harness.castInstant(player1, 0, List.of(bears.getId(), mercy.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, initialLife);
    }

    @Test
    @DisplayName("Cannot choose more than two target spells")
    void cannotChooseThreeTargets() {
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        stackTwoSpells(bears, mercy);
        DoubleNegative thirdSpell = new DoubleNegative();
        harness.setHand(player1, List.of(thirdSpell, new DoubleNegative()));
        harness.castInstant(player1, 0, List.of());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(bears.getId(), mercy.getId(), thirdSpell.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An uncounterable target does not prevent countering the other spell")
    void countersOtherSpellAlongsideUncounterableTarget() {
        SpellbreakerBehemoth behemoth = new SpellbreakerBehemoth();
        DoubleNegative firstCounter = new DoubleNegative();
        harness.setHand(player1, List.of(behemoth, firstCounter, new DoubleNegative()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, List.of());

        harness.castAndResolveInstant(player1, 0, List.of(behemoth.getId(), firstCounter.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(behemoth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCounter);
        harness.assertNotInGraveyard(player1, "Spellbreaker Behemoth");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbreaker Behemoth");
    }
}
