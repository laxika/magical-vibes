package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GwennaEyesOfGaea;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HajarLoyalBodyguard.class, AdelizTheCinderWind.class, GrizzlyBears.class, GwennaEyesOfGaea.class})
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

    @Test
    void tappedSummoningSickHajarCanBeSacrificedAndPaysCostBeforeResolution() {
        Permanent hajar = harness.addToBattlefieldAndReturn(player1, new HajarLoyalBodyguard());
        hajar.setSummoningSick(true);
        hajar.tap();
        Permanent legendary = addCreatureReady(player1, new GwennaEyesOfGaea());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Hajar, Loyal Bodyguard");
        harness.assertInGraveyard(player1, "Hajar, Loyal Bodyguard");
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void legendaryCreatureEnteringBeforeResolutionIsAffected() {
        addCreatureReady(player1, new HajarLoyalBodyguard());
        harness.activateAbility(player1, 0, null, null);

        Permanent legendary = harness.enterBattlefieldAndReturn(player1, new GwennaEyesOfGaea());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void legendaryCreatureEnteringAfterResolutionIsNotAffected() {
        addCreatureReady(player1, new HajarLoyalBodyguard());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent legendary = harness.enterBattlefieldAndReturn(player1, new GwennaEyesOfGaea());

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
