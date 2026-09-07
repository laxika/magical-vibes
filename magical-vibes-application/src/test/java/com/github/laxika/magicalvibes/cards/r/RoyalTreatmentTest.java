package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RoyalTreatment.class, GrizzlyBears.class})
class RoyalTreatmentTest extends BaseCardTest {

    @Test
    @DisplayName("Grants hexproof and attaches a Royal Role")
    void grantsHexproofAndRoyalRole() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castRoyalTreatment(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo())))
                .hasSize(1);
    }

    @Test
    @DisplayName("Hexproof wears off at end of turn while the Role remains")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castRoyalTreatment(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo())))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoyalTreatment()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castRoyalTreatment(Permanent target) {
        harness.setHand(player1, List.of(new RoyalTreatment()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
