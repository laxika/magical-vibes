package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessBushwhacker.class, GrizzlyBears.class, Shock.class})
class RecklessBushwhackerTest extends BaseCardTest {

    @Test
    @DisplayName("Surge gives other creatures you control +1/+0 and haste until end of turn")
    void surgeBoostsOtherCreaturesYouControl() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new RecklessBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.passBothPriorities();
        Permanent bushwhacker = findPermanent(player1, "Reckless Bushwhacker");
        assertThat(ally.getPowerModifier()).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(bushwhacker.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(ally.getPowerModifier()).isZero();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Normal casting does not trigger the surge ability")
    void normalCastDoesNotBoostCreatures() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RecklessBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isZero();
        assertThat(ally.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Surge is unavailable before another spell is cast")
    void surgeRequiresAnotherSpell() {
        harness.setHand(player1, List.of(new RecklessBushwhacker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
