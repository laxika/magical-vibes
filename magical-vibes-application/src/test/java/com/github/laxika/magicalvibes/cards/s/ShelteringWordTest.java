package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

class ShelteringWordTest extends BaseCardTest {

    @Test
    @DisplayName("Grants hexproof and gains life equal to the target's toughness")
    void grantsHexproofAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        castResolve(bears);

        assertThat(bears.getGrantedKeywords()).contains(Keyword.HEXPROOF);
        // Grizzly Bears is 2/2 -> gain 2 life
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId()))
                .isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Life gained scales with the target creature's toughness")
    void lifeGainScalesWithToughness() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        castResolve(elemental);

        // Air Elemental is 4/4 -> gain 4 life
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId()))
                .isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Hexproof wears off at end of turn")
    void hexproofWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castResolve(bears);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.HEXPROOF);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteringWord()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID opponentId = opponent.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new ShelteringWord()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
