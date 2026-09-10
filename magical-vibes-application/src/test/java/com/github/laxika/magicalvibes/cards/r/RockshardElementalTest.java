package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RockshardElemental.class)
class RockshardElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals combat damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent elemental = addCreatureReady(player1, new RockshardElemental());
        elemental.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canMorphFaceDownAndFaceUp() {
        harness.setHand(player1, List.of(new RockshardElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Rockshard Elemental");
        assertThat(elemental.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental));
        harness.passBothPriorities();

        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
