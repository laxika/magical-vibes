package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DarklingStalker;
import com.github.laxika.magicalvibes.cards.e.EvincarsJustice;
import com.github.laxika.magicalvibes.cards.s.SeleniaDarkAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JetMedallion.class, DarklingStalker.class, CanopySpider.class, SeleniaDarkAngel.class,
        EvincarsJustice.class})
class JetMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Black spells you cast cost {1} less")
    void blackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JetMedallion());
        // Darkling Stalker costs {3}{B} — with the {1} reduction it should cost {2}{B}
        harness.setHand(player1, List.of(new DarklingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Darkling Stalker"));
    }

    @Test
    @DisplayName("Black multicolored spells you cast cost {1} less")
    void multicoloredBlackSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JetMedallion());
        // Selenia costs {3}{W}{B}; its black color qualifies despite being multicolored.
        harness.setHand(player1, List.of(new SeleniaDarkAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Selenia, Dark Angel"));
    }

    @Test
    @DisplayName("Non-black spells are not reduced")
    void nonBlackSpellsNotReduced() {
        harness.addToBattlefield(player1, new JetMedallion());
        // Canopy Spider costs {1}{G} — not black, so only {G} is not enough
        harness.setHand(player1, List.of(new CanopySpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new JetMedallion());
        harness.setHand(player2, List.of(new DarklingStalker()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleMedallionsStack() {
        harness.addToBattlefield(player1, new JetMedallion());
        harness.addToBattlefield(player1, new JetMedallion());
        harness.setHand(player1, List.of(new DarklingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkling Stalker");
    }

    @Test
    void excessReductionDoesNotRemoveColoredRequirements() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new JetMedallion());
        }
        harness.setHand(player1, List.of(new DarklingStalker()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkling Stalker");
    }

    @Test
    void tappedMedallionStillReducesCosts() {
        harness.addToBattlefieldAndReturn(player1, new JetMedallion()).tap();
        harness.setHand(player1, List.of(new DarklingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darkling Stalker");
    }

    @Test
    void reductionAppliesToEverySpell() {
        harness.addToBattlefield(player1, new JetMedallion());
        harness.setHand(player1, List.of(new DarklingStalker(), new DarklingStalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Darkling Stalker"))
                .hasSize(2);
    }

    @Test
    void noncreatureSpellWithBuybackGetsOneReductionForTotalCost() {
        harness.addToBattlefield(player1, new JetMedallion());
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Evincar's Justice");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void reductionIsNotAppliedAgainToBuyback() {
        harness.addToBattlefield(player1, new JetMedallion());
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionBeyondPrintedGenericCostAppliesToBuyback() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new JetMedallion());
        }
        harness.setHand(player1, List.of(new EvincarsJustice()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Evincar's Justice");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
