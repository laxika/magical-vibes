package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({HengeGuardian.class, HengeOfRamos.class, PowerMatrix.class, Toymaker.class})
class ToymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card and animates a noncreature artifact with P/T equal to its mana value")
    void animatesArtifactWithManaValuePt() {
        Permanent toymaker = prepareToymaker();
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());

        activateToymaker(matrix);

        assertThat(toymaker.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.isCreature(gd, matrix)).isTrue();
        assertThat(gqs.getEffectivePower(gd, matrix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, matrix)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, matrix)).isTrue();
        harness.assertInGraveyard(player1, "Toymaker");
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        prepareToymaker();
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());

        activateToymaker(matrix);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, matrix)).isFalse();
        assertThat(gqs.isArtifact(gd, matrix)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        prepareToymaker();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HengeGuardian());
        prepareDiscardAndMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        prepareToymaker();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HengeOfRamos());
        prepareDiscardAndMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Can target an opponent's noncreature artifact")
    void canTargetOpponentsArtifact() {
        prepareToymaker();
        Permanent matrix = harness.addToBattlefieldAndReturn(player2, new PowerMatrix());
        activateToymaker(matrix);

        assertThat(gqs.isCreature(gd, matrix)).isTrue();
        assertThat(gqs.isArtifact(gd, matrix)).isTrue();
        assertThat(gqs.getEffectivePower(gd, matrix)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, matrix)).isEqualTo(4);
    }

    @Test
    @DisplayName("Retains the animated artifact's abilities")
    void retainsAbilities() {
        prepareToymaker();
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());
        matrix.setSummoningSick(false);
        Permanent guardian = addCreatureReady(player1, new HengeGuardian());
        activateToymaker(matrix);

        harness.activateAbility(player1, 1, 0, null, guardian.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        prepareToymaker();
        Permanent matrix = harness.addToBattlefieldAndReturn(player1, new PowerMatrix());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, matrix.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent prepareToymaker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return addCreatureReady(player1, new Toymaker());
    }

    private void prepareDiscardAndMana() {
        harness.setHand(player1, List.of(new Toymaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateToymaker(Permanent target) {
        prepareDiscardAndMana();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
