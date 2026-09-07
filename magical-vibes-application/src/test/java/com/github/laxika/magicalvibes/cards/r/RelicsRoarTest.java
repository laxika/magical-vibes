package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicsRoar.class, GrizzlyBears.class, Spellbook.class, Forest.class})
class RelicsRoarTest extends BaseCardTest {

    @Test
    void turnsCreatureIntoDinosaurArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAt(target);

        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getTransientSubtypes()).contains(CardSubtype.DINOSAUR);
    }

    @Test
    void turnsArtifactIntoDinosaurArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        castAt(target);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getTransientSubtypes()).contains(CardSubtype.DINOSAUR);
    }

    @Test
    void animationEndsAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAt(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(target)).isFalse();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(target.getTransientSubtypes()).doesNotContain(CardSubtype.DINOSAUR);
    }

    @Test
    void cannotTargetNonArtifactOrCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RelicsRoar()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new RelicsRoar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
