package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FencerClique;
import com.github.laxika.magicalvibes.cards.k.KithkinZephyrnaut;
import com.github.laxika.magicalvibes.cards.o.OrderOfTheGoldenCricket;
import com.github.laxika.magicalvibes.cards.s.StonybrookSchoolmaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BallyrushBanneret.class, FencerClique.class, KithkinZephyrnaut.class,
        OrderOfTheGoldenCricket.class, StonybrookSchoolmaster.class})
class BallyrushBanneretTest extends BaseCardTest {

    @Test
    @DisplayName("Kithkin spells cost {1} less with Ballyrush Banneret on the battlefield")
    void kithkinSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Order of the Golden Cricket costs {1}{W}; the reduction leaves {W}.
        harness.setHand(player1, List.of(new OrderOfTheGoldenCricket()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Kithkin spell still requires colored mana after generic reduction")
    void kithkinSpellStillRequiresColoredMana() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // The reduction does not remove Order of the Golden Cricket's {W} requirement.
        harness.setHand(player1, List.of(new OrderOfTheGoldenCricket()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Soldier spells cost {1} less with Ballyrush Banneret on the battlefield")
    void soldierSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Fencer Clique costs {2}{U}{U}; the reduction leaves {1}{U}{U}.
        harness.setHand(player1, List.of(new FencerClique()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Kithkin, non-Soldier spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Stonybrook Schoolmaster costs {2}{W}; two white is not enough without a reduction.
        harness.setHand(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Bannerets reduce a Kithkin spell's cost by {2}")
    void twoBanneretsStackReduction() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Order of the Golden Cricket costs {1}{W}; two reductions remove its generic cost.
        harness.setHand(player1, List.of(new OrderOfTheGoldenCricket()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Kithkin Soldier spell is reduced only once")
    void matchingBothSubtypesIsReducedOnlyOnce() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Kithkin Zephyrnaut costs {2}{W}; one Banneret leaves {1}{W}, not just {W}.
        harness.setHand(player1, List.of(new KithkinZephyrnaut()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ballyrush Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // The opponent's Order of the Golden Cricket still costs {1}{W}; one white is not enough.
        harness.setHand(player2, List.of(new OrderOfTheGoldenCricket()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
