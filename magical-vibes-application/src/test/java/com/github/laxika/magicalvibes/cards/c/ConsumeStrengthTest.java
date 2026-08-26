package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumeStrength.class, GrizzlyBears.class, HillGiant.class})
class ConsumeStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Applies +2/+2 to the first target and -2/-2 to another target")
    void appliesBothEffectsToDistinctTargets() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castConsumeStrength(boosted, weakened);

        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, weakened)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, weakened)).isEqualTo(1);
    }

    @Test
    @DisplayName("The effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castConsumeStrength(boosted, weakened);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, weakened)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, weakened)).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects the same creature as both targets")
    void rejectsSharedTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsumeStrength()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castConsumeStrength(Permanent boosted, Permanent weakened) {
        harness.setHand(player1, List.of(new ConsumeStrength()));
        addMana();
        harness.castInstant(player1, 0, List.of(boosted.getId(), weakened.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
