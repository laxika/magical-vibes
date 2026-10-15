package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OvergrownArmasaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfTheStampede.class, FrenziedRaptor.class, GrizzlyBears.class, OvergrownArmasaur.class})
class KnightOfTheStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur spells cost {2} less to cast")
    void dinosaurSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Frenzied Raptor");
    }

    @Test
    @DisplayName("Non-Dinosaur creature spells are not reduced")
    void nonDinosaurSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction does not affect an opponent's Dinosaur spells")
    void doesNotReduceOpponentDinosaurSpells() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player2, List.of(new FrenziedRaptor()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Dinosaur with more than two generic mana still requires the remainder")
    void remainingGenericManaMustBePaid() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player1, List.of(new OvergrownArmasaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Knights cumulatively reduce generic mana without a minimum generic cost")
    void multipleKnightsReduceCostCumulatively() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player1, List.of(new OvergrownArmasaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Overgrown Armasaur");
    }

    @Test
    @DisplayName("Excess generic reduction does not pay a Dinosaur's colored mana cost")
    void excessReductionDoesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.addToBattlefield(player1, new KnightOfTheStampede());
        harness.setHand(player1, List.of(new OvergrownArmasaur()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Knight in the graveyard does not reduce casting costs")
    void knightInGraveyardDoesNotReduceCosts() {
        harness.setGraveyard(player1, List.of(new KnightOfTheStampede()));
        harness.setHand(player1, List.of(new OvergrownArmasaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
