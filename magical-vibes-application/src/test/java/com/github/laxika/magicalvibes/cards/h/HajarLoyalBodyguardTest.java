package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HajarLoyalBodyguardTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndAffectsOnlyOwnLegendaryCreatures() {
        addCreatureReady(player1, new HajarLoyalBodyguard());
        Permanent ownLegendary = addCreatureReady(player1, new AdelizTheCinderWind());
        Permanent ownNonLegendary = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentLegendary = addCreatureReady(player2, new AdelizTheCinderWind());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hajar, Loyal Bodyguard");
        assertThat(gqs.getEffectivePower(gd, ownLegendary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownLegendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownLegendary, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownNonLegendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownNonLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentLegendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void boostAndIndestructibleWearOffAtEndOfTurn() {
        addCreatureReady(player1, new HajarLoyalBodyguard());
        Permanent ownLegendary = addCreatureReady(player1, new AdelizTheCinderWind());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownLegendary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownLegendary, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownLegendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
