package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercurialTransformationTest extends BaseCardTest {

    private static final String FROG_MODE = "Become a blue Frog creature with base power and toughness 1/1";
    private static final String OCTOPUS_MODE = "Become a blue Octopus creature with base power and toughness 4/4";

    @Test
    @DisplayName("Resolution-time Frog mode transforms a noncreature permanent")
    void frogModeTransformsNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());

        castAndChoose(artifact, FROG_MODE);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, artifact)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifact)).containsExactly(CardSubtype.FROG);
        assertThat(gqs.computeStaticBonus(gd, artifact).losesAllAbilities()).isTrue();
    }

    @Test
    @DisplayName("Resolution-time Octopus mode replaces the target's creature type")
    void octopusModeReplacesCreatureType() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndChoose(bears, OCTOPUS_MODE);

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.OCTOPUS);
    }

    @Test
    @DisplayName("Mercurial Transformation cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new MercurialTransformation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Transformation effects expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        castAndChoose(artifact, FROG_MODE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, artifact).losesAllAbilities()).isFalse();
    }

    private void castAndChoose(Permanent target, String mode) {
        harness.setHand(player1, List.of(new MercurialTransformation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
