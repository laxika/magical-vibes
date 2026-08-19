package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EyeOfMalcatorTest extends BaseCardTest {

    @Test
    @DisplayName("Eye of Malcator's enters-the-battlefield ability starts a scry 2 interaction")
    void entersTheBattlefieldScriesTwo() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EyeOfMalcator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Another artifact entering under your control animates Eye of Malcator")
    void anotherAllyArtifactEntryAnimatesEye() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, eye)).isTrue();
        assertThat(gqs.isArtifact(eye)).isTrue();
        assertThat(gqs.getEffectivePower(gd, eye)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eye)).isEqualTo(4);
        assertThat(eye.getTransientSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.EYE);
    }

    @Test
    @DisplayName("Eye of Malcator's animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent eye = harness.addToBattlefieldAndReturn(player1, new EyeOfMalcator());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, eye)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, eye)).isFalse();
        assertThat(eye.getTransientSubtypes()).isEmpty();
    }
}
