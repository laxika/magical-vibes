package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IymrithDesertDoom.class, GiantGrowth.class, GrizzlyBears.class})
class IymrithDesertDoomTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped Iymrith has ward 4")
    void untappedIymrithHasWardFour() {
        Permanent iymrith = addReadyIymrith();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castInstant(player2, 0, iymrith.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, iymrith)).isEqualTo(5);
    }

    @Test
    @DisplayName("Tapped Iymrith does not have ward")
    void tappedIymrithDoesNotHaveWard() {
        Permanent iymrith = addReadyIymrith();
        iymrith.tap();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, iymrith.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, iymrith)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, iymrith)).isEqualTo(8);
    }

    @Test
    @DisplayName("Combat damage draws up to three cards based on the resulting hand size")
    void combatDamageDrawsToThreeCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent iymrith = addReadyIymrith();
        iymrith.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyIymrith() {
        Permanent iymrith = harness.addToBattlefieldAndReturn(player1, new IymrithDesertDoom());
        iymrith.setSummoningSick(false);
        return iymrith;
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
