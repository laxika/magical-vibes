package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CamaraderieTest extends BaseCardTest {

    @Test
    @DisplayName("Gains and draws one card per creature you control, then boosts your creatures")
    void resolvesAllEffectsUsingOwnCreatureCount() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownBearTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(ownBear.getEffectivePower()).isEqualTo(3);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownBearTwo.getEffectivePower()).isEqualTo(3);
        assertThat(ownBearTwo.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentBear.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The team boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        cast();

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    private void cast() {
        harness.setHand(player1, List.of(new Camaraderie()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
