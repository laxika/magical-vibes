package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pyrewood Gearhulk")
class PyrewoodGearhulkTest extends BaseCardTest {

    private void castGearhulk() {
        harness.setHand(player1, List.of(new PyrewoodGearhulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB boosts and grants vigilance and menace to other creatures you control")
    void etbBoostsOtherOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGearhulk();

        Permanent gearhulk = findPermanent(player1, "Pyrewood Gearhulk");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gearhulk.getPowerModifier()).isZero();
        assertThat(gearhulk.getToughnessModifier()).isZero();
        assertThat(opponentBears.getPowerModifier()).isZero();
        assertThat(opponentBears.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and keyword grants wear off at end of turn")
    void etbEffectsWearOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGearhulk();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("ETB makes damage unpreventable for the rest of the turn")
    void damageCannotBePreventedThisTurn() {
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        castGearhulk();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }
}
