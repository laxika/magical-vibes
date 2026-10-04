package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EndlessOne;
import com.github.laxika.magicalvibes.cards.k.KozileksSentinel;
import com.github.laxika.magicalvibes.cards.m.MakindiSliderunner;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfKozilek.class, KozileksSentinel.class, MakindiSliderunner.class, EndlessOne.class, HedronArchive.class, MycosynthLattice.class})
class HeraldOfKozilekTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless spells you cast cost {1} less to cast")
    void colorlessSpellsCostOneLess() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new KozileksSentinel()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Colored spells are not reduced")
    void coloredSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new MakindiSliderunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to an opponent's colorless spells")
    void opponentColorlessSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player2, List.of(new KozileksSentinel()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Heralds stack reductions and preserve the chosen X")
    void stackedReductionsPreserveChosenX() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new EndlessOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Endless One").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Generic costs can be reduced to zero")
    void genericCostCanBeReducedToZero() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new EndlessOne()));

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Endless One").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Excess reduction does not pay colored mana requirements")
    void excessReductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new HeraldOfKozilek()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Herald does not reduce its own cost from hand")
    void heraldDoesNotReduceItselfFromHand() {
        harness.setHand(player1, List.of(new HeraldOfKozilek()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Herald in the graveyard does not reduce spells")
    void heraldInGraveyardDoesNotReduceSpells() {
        harness.setGraveyard(player1, List.of(new HeraldOfKozilek()));
        harness.setHand(player1, List.of(new KozileksSentinel()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colorless noncreature spells also receive the reduction")
    void colorlessArtifactCostsOneLess() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.setHand(player1, List.of(new HedronArchive()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hedron Archive");
    }

    @Test
    @DisplayName("Cards made colorless by Mycosynth Lattice receive the reduction")
    void latticeMakesColoredSpellsEligible() {
        harness.addToBattlefield(player1, new HeraldOfKozilek());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new MakindiSliderunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Makindi Sliderunner");
    }
}
