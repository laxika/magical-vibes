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

        assertThat(own.getEffectivePower()).isEqualTo(7);
        assertThat(own.getEffectiveToughness()).isEqualTo(7);
        assertThat(own.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(own.hasKeyword(Keyword.LIFELINK)).isTrue();

        assertThat(enemy.getEffectivePower()).isEqualTo(2);
        assertThat(enemy.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(enemy.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(enemy.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOff() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        castUltimatum();

        assertThat(own.getEffectivePower()).isEqualTo(7);
        assertThat(own.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(own.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(own.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the effects")
    void laterCreaturesAreUnaffected() {
        castUltimatum();
        Permanent late = harness.enterBattlefieldAndReturn(player1, new CylianElf());

        assertThat(late.getEffectivePower()).isEqualTo(2);
        assertThat(late.getEffectiveToughness()).isEqualTo(2);
        assertThat(late.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(late.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(late.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before resolution receive all the effects")
    void creaturesPresentAtResolutionAreAffected() {
        harness.castFromHand(player1, new TitanicUltimatum(), "{R}{R}{G}{G}{G}{W}{W}");
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new CylianElf());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(7);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    private void castUltimatum() {
        harness.castFromHand(player1, new TitanicUltimatum(), "{R}{R}{G}{G}{G}{W}{W}");
        harness.passBothPriorities();
    }
}
