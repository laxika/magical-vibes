package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.h.HengeOfRamos;
import com.github.laxika.magicalvibes.cards.p.PowerMatrix;
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

@CardUsed({HengeGuardian.class, HengeOfRamos.class, KarnsTouch.class, PowerMatrix.class})
class KarnsTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a target noncreature artifact with P/T equal to its mana value")
    void animatesNoncreatureArtifact() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());
        castKarnsTouch(matrix);

        assertThat(gqs.isCreature(gd, matrix)).isTrue();
        assertThat(gqs.isArtifact(gd, matrix)).isTrue();
        assertThat(gqs.getEffectivePower(gd, matrix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, matrix)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new HengeGuardian());
        prepareKarnsTouch();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, guardian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new HengeOfRamos());
        prepareKarnsTouch();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Can target an opponent's noncreature artifact")
    void canTargetOpponentsArtifact() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player2, new PowerMatrix());
        castKarnsTouch(matrix);

        assertThat(gqs.isCreature(gd, matrix)).isTrue();
        assertThat(gqs.isArtifact(gd, matrix)).isTrue();
        assertThat(gqs.getEffectivePower(gd, matrix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, matrix)).isEqualTo(4);
    }

    @Test
    @DisplayName("Retains the animated artifact's abilities")
    void retainsAbilities() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());
        matrix.setSummoningSick(false);
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new HengeGuardian());
        castKarnsTouch(matrix);

        harness.activateAbility(player1, 0, null, guardian.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());
        castKarnsTouch(matrix);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, matrix)).isFalse();
        assertThat(gqs.isArtifact(gd, matrix)).isTrue();
    }

    private void castKarnsTouch(Permanent target) {
        prepareKarnsTouch();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareKarnsTouch() {
        harness.setHand(player1, List.of(new KarnsTouch()));
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
