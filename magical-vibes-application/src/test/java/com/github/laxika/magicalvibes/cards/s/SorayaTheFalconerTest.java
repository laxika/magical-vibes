package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.m.MesaFalcon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorayaTheFalconer.class, MesaFalcon.class, SpectralBears.class, AmoeboidChangeling.class})
class SorayaTheFalconerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts Bird creatures controlled by either player")
    void boostsBirdsEverywhere() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent ownBird = harness.addToBattlefieldAndReturn(player1, new MesaFalcon());
        Permanent enemyBird = harness.addToBattlefieldAndReturn(player2, new MesaFalcon());

        assertThat(gqs.getEffectivePower(gd, ownBird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBird)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enemyBird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBird)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-Bird creatures")
    void doesNotBoostNonBirds() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SpectralBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Grants banding to a target Bird until end of turn")
    void grantsBandingToBird() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new MesaFalcon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, bird.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bird, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bird, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Bird creature")
    void cannotTargetNonBird() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SpectralBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant banding if the target stops being a Bird before resolution")
    void targetMustStillBeBirdAtResolution() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new MesaFalcon());
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, bird.getId());
        harness.activateAbility(player1, 2, 1, null, bird.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bird, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Can grant banding to an opponent's Bird")
    void grantsBandingToOpponentBird() {
        harness.addToBattlefield(player1, new SorayaTheFalconer());
        Permanent opponentBird = harness.addToBattlefieldAndReturn(player2, new MesaFalcon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, opponentBird.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentBird, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("A Bird version of Soraya also receives the Bird boost")
    void boostsItselfWhenItBecomesABird() {
        Permanent soraya = harness.addToBattlefieldAndReturn(player1, new SorayaTheFalconer());
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, 0, null, soraya.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soraya)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soraya)).isEqualTo(3);
    }
}
