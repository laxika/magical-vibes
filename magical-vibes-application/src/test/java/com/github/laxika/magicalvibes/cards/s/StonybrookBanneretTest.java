package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Fertilid;
import com.github.laxika.magicalvibes.cards.i.InspiredSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonybrookBanneret.class, StonybrookSchoolmaster.class, InspiredSprite.class, Fertilid.class,
        SagesDousing.class, ElvishWarrior.class})
class StonybrookBanneretTest extends BaseCardTest {

    @Test
    @DisplayName("Merfolk spells cost {1} less with Stonybrook Banneret on the battlefield")
    void merfolkSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Stonybrook Schoolmaster (Merfolk Wizard) costs {2}{W}; with {1} reduction, {1}{W} suffices.
        harness.castFromHand(player1, new StonybrookSchoolmaster(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Stonybrook Schoolmaster");
    }

    @Test
    @DisplayName("Merfolk spell is not castable when mana falls short of the reduced cost")
    void merfolkNotCastableWithoutEnoughMana() {
        // Without the Banneret, Stonybrook Schoolmaster costs {2}{W}; two white is not enough.
        harness.setHand(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wizard spells cost {1} less with Stonybrook Banneret on the battlefield")
    void wizardSpellsCostOneLess() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Inspired Sprite (Faerie Wizard) costs {3}{U}; with {1} reduction, {2}{U} suffices.
        harness.castFromHand(player1, new InspiredSprite(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Inspired Sprite");
    }

    @Test
    @DisplayName("Wizard noncreature spells also cost {1} less")
    void wizardNoncreatureSpellsCostOneLess() {
        harness.addToBattlefield(player2, new StonybrookBanneret());

        ElvishWarrior warrior = new ElvishWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        // Sage's Dousing is a Kindred Instant — Wizard and costs {2}{U}; with the Banneret, {1}{U} suffices.
        harness.setHand(player2, List.of(new SagesDousing()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, warrior.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).extracting(entry -> entry.getCard().getName())
                .containsExactlyInAnyOrder("Elvish Warrior", "Sage's Dousing");
    }

    @Test
    @DisplayName("A Merfolk Wizard spell is reduced only once")
    void matchingBothSubtypesIsReducedOnlyOnce() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Stonybrook Schoolmaster costs {2}{W}; one Banneret reduces it to {1}{W}, not just {W}.
        harness.setHand(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Merfolk, non-Wizard spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // Fertilid (Elemental) costs {2}{G}; one green is not enough without a reduction.
        harness.setHand(player1, List.of(new Fertilid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Stonybrook Bannerets reduce a matching spell by {2}")
    void twoBanneretsStackReduction() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        harness.addToBattlefield(player1, new StonybrookBanneret());

        // Stonybrook Schoolmaster costs {2}{W}; two reductions leave only {W}.
        harness.castFromHand(player1, new StonybrookSchoolmaster(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Stonybrook Schoolmaster");
    }

    @Test
    @DisplayName("Stonybrook Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        // The opponent's Stonybrook Schoolmaster still costs {2}{W}; two white is not enough.
        harness.setHand(player2, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
