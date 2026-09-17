package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({ScaleUp.class, GrizzlyBears.class, SerraAngel.class, FountainOfYouth.class})
class ScaleUpTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control becomes a green 6/4 Wurm")
    void scalesUpTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player1, List.of(new ScaleUp()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.WURM);
    }

    @Test
    @DisplayName("Scale Up's changes wear off at end of turn")
    void changesWearOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castNormally(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.ANGEL);
    }

    @Test
    @DisplayName("Overloaded Scale Up affects every creature you control")
    void overloadAffectsAllYourCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ScaleUp()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        for (Permanent ownCreature : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
            assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.GREEN);
            assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).containsExactly(CardSubtype.WURM);
        }
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, theirs)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Scale Up cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ScaleUp()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castNormally(Permanent target) {
        harness.setHand(player1, List.of(new ScaleUp()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
