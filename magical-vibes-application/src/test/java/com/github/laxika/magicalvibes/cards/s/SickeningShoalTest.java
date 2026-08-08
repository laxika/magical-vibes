package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SickeningShoalTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -X/-X and dies when toughness drops to zero")
    void killsCreatureWithMatchingX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 2, bearId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that survives keeps the -X/-X until end of turn")
    void shrinksSurvivingCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, 2, giantId);
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -X/-X wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, 2, giantId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiling a black card with mana value X pays the alternative cost")
    void alternativeCostExilesBlackCardWithManaValueX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        // Sickening Shoal's own mana value is 2, so exiling one pays for X = 2 with no mana spent.
        harness.setHand(player1, List.of(new SickeningShoal(), new SickeningShoal()));

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstantWithAlternateExileFromHand(player1, 0, 2, bearId, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exiled card's mana value must equal the chosen X")
    void alternativeCostRejectsMismatchedManaValue() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SickeningShoal(), new SickeningShoal()));

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, 3, bearId, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID landId = harness.getPermanentId(player1, "Swamp");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
