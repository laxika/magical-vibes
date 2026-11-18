package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitanicUltimatum.class, CylianElf.class})
class TitanicUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Gives own creatures +5/+5 and first strike, trample, lifelink; opponent's untouched")
    void buffsOwnCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        castUltimatum();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, own, Keyword.LIFELINK)).isTrue();

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, enemy, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, enemy, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, enemy, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOff() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        castUltimatum();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, own, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, own, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, own, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the effects")
    void laterCreaturesAreUnaffected() {
        castUltimatum();
        Permanent late = harness.enterBattlefieldAndReturn(player1, new CylianElf());

        assertThat(gqs.getEffectivePower(gd, late)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, late)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, late, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, late, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, late, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before resolution receive all the effects")
    void creaturesPresentAtResolutionAreAffected() {
        harness.castFromHand(player1, new TitanicUltimatum(), "{R}{R}{G}{G}{G}{W}{W}");
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new CylianElf());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    private void castUltimatum() {
        harness.castFromHand(player1, new TitanicUltimatum(), "{R}{R}{G}{G}{G}{W}{W}");
        harness.passBothPriorities();
    }
}
