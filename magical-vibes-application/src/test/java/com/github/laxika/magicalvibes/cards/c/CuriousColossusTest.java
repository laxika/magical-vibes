package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuriousColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens all creatures controlled by the targeted opponent and adds Coward")
    void affectsTargetOpponentsCreatures() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castCuriousColossus(player2.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(1);
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel))
                .contains(CardSubtype.ANGEL, CardSubtype.COWARD);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.COWARD);
        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The changes wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castCuriousColossus(player2.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel))
                .contains(CardSubtype.ANGEL)
                .doesNotContain(CardSubtype.COWARD);
    }

    @Test
    @DisplayName("Cannot target its own controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new CuriousColossus()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCuriousColossus(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CuriousColossus()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castCreature(player1, 0, List.of(targetPlayerId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
