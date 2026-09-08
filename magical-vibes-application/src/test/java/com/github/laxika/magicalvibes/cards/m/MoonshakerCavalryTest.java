package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MoonshakerCavalry.class, GrizzlyBears.class})
class MoonshakerCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives your creatures flying and a boost equal to your creature count")
    void etbCountsCreaturesYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMoonshakerCavalry();

        Permanent moonshaker = findPermanent(player1, "Moonshaker Cavalry");
        assertThat(ownCreature.getEffectivePower()).isEqualTo(5);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(secondOwnCreature.getEffectivePower()).isEqualTo(5);
        assertThat(secondOwnCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(moonshaker.getEffectivePower()).isEqualTo(9);
        assertThat(moonshaker.getEffectiveToughness()).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondOwnCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The temporary boost and flying grant wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castMoonshakerCavalry();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    private void castMoonshakerCavalry() {
        harness.setHand(player1, List.of(new MoonshakerCavalry()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
