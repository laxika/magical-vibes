package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
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

@CardUsed({GempalmAvenger.class, AvenEnvoy.class, FugitiveWizard.class})
class GempalmAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling gives Soldier creatures +1/+1 and first strike")
    void cyclingBoostsSoldiersEverywhere() {
        Permanent ownSoldier = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new AvenEnvoy());
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GempalmAvenger()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSoldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownSoldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownSoldier, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentSoldier, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonSoldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonSoldier)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonSoldier, Keyword.FIRST_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Gempalm Avenger");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cycling's Soldier boost and first strike wear off at end of turn")
    void cyclingEffectsWearOffAtEndOfTurn() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        harness.setHand(player1, List.of(new GempalmAvenger()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
