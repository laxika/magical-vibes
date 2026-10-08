package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Fertilid;
import com.github.laxika.magicalvibes.cards.g.GrimoireThief;
import com.github.laxika.magicalvibes.cards.i.InspiredSprite;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonybrookBanneret.class, StonybrookSchoolmaster.class, InspiredSprite.class, Fertilid.class,
        SagesDousing.class, ElvishWarrior.class, GrimoireThief.class, Island.class})
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
    @DisplayName("Without a Banneret, Merfolk spells require their full cost")
    void merfolkNotCastableWithoutBanneret() {
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
        harness.castFromHand(player1, warrior, "{G}{G}");
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
        // Fertilid costs {2}{G}; {G}{G} would suffice only if it incorrectly received a reduction.
        harness.setHand(player1, List.of(new Fertilid()));
        harness.addMana(player1, ManaColor.GREEN, 2);

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
        harness.forceActivePlayer(player2);
        // The opponent's Stonybrook Schoolmaster still costs {2}{W}; two white is not enough.
        harness.setHand(player2, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void excessReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        harness.addToBattlefield(player1, new StonybrookBanneret());
        harness.setHand(player1, List.of(new GrimoireThief()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void banneretInHandDoesNotReduceItsOwnCost() {
        harness.setHand(player1, List.of(new StonybrookBanneret()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anotherBanneretReducesBanneretSpell() {
        harness.addToBattlefield(player1, new StonybrookBanneret());
        harness.castFromHand(player1, new StonybrookBanneret(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void islandwalkPreventsBlockingWithDefendingIsland() {
        Permanent attacker = addCreatureReady(player1, new StonybrookBanneret());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new Island());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void islandwalkDoesNotCheckAttackersIsland() {
        Permanent attacker = addCreatureReady(player1, new StonybrookBanneret());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
