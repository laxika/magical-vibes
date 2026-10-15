package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LatchkeyFaerie;
import com.github.laxika.magicalvibes.cards.m.MorselTheft;
import com.github.laxika.magicalvibes.cards.s.SqueakingPieGrubfellows;
import com.github.laxika.magicalvibes.cards.s.StinkdrinkerBandit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrogtosserBanneret.class, Fertilid.class, LatchkeyFaerie.class, MorselTheft.class,
        SqueakingPieGrubfellows.class, StinkdrinkerBandit.class})
class FrogtosserBanneretTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin spells cost {1} less with Frogtosser Banneret on the battlefield")
    void goblinSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Squeaking Pie Grubfellows costs {3}{B}; the reduction leaves {2}{B}.
        harness.castFromHand(player1, new SqueakingPieGrubfellows(), "{2}{B}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Goblin spell is not castable when mana falls short of the reduced cost")
    void goblinNotCastableWithoutEnoughMana() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Squeaking Pie Grubfellows is reduced to {2}{B}; only {1}{B} is available.
        assertThatThrownBy(() -> harness.castFromHand(player1, new SqueakingPieGrubfellows(), "{1}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rogue spells cost {1} less with Frogtosser Banneret on the battlefield")
    void rogueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Latchkey Faerie costs {3}{U}; the reduction leaves {2}{U}.
        harness.castFromHand(player1, new LatchkeyFaerie(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Goblin, non-Rogue spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Fertilid costs {2}{G}; without a reduction, {1}{G} is not enough.
        assertThatThrownBy(() -> harness.castFromHand(player1, new Fertilid(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Bannerets reduce a Goblin spell's cost by {2}")
    void twoBanneretsStackReduction() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Squeaking Pie Grubfellows costs {3}{B}; two reductions leave {1}{B}.
        harness.castFromHand(player1, new SqueakingPieGrubfellows(), "{1}{B}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Goblin Rogue spell is reduced only once when it matches both subtypes")
    void overlappingSubtypesAreReducedOnlyOnce() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Stinkdrinker Bandit costs {3}{B}; one reduction still requires {2}{B}.
        assertThatThrownBy(() -> harness.castFromHand(player1, new StinkdrinkerBandit(), "{1}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kindred Rogue spells also receive the cost reduction")
    void noncreatureRogueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // Morsel Theft costs {2}{B}{B}; the reduction leaves {1}{B}{B}.
        harness.setHand(player1, List.of(new MorselTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Frogtosser Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        // The opponent's Squeaking Pie Grubfellows still costs {3}{B}; only {2}{B} is available.
        assertThatThrownBy(() -> harness.castFromHand(player2, new SqueakingPieGrubfellows(), "{2}{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Excess reduction leaves the colored mana requirement intact")
    void excessReductionDoesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        harness.addToBattlefield(player1, new FrogtosserBanneret());

        assertThatThrownBy(() -> harness.castFromHand(player1, new FrogtosserBanneret(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Excess reduction can reduce the generic component to zero")
    void excessReductionStopsAtZeroGenericMana() {
        harness.addToBattlefield(player1, new FrogtosserBanneret());
        harness.addToBattlefield(player1, new FrogtosserBanneret());

        harness.castFromHand(player1, new FrogtosserBanneret(), "{B}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Banneret in hand cannot reduce its own casting cost")
    void banneretDoesNotReduceItsOwnCostFromHand() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new FrogtosserBanneret(), "{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Frogtosser Banneret can attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        var banneret = harness.addToBattlefieldAndReturn(player1, new FrogtosserBanneret());

        assertThat(als.canAttack(gd, banneret, player1.getId())).isTrue();
    }
}
