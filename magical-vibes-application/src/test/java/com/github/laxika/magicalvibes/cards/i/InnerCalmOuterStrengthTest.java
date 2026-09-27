package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
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

@CardUsed({InnerCalmOuterStrength.class, ArabaMothrider.class, OboroPalaceInTheClouds.class})
class InnerCalmOuterStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +X/+X based on cards remaining in the caster's hand")
    void boostsByCardsRemainingInHand() {
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(
                new InnerCalmOuterStrength(),
                new InnerCalmOuterStrength(),
                new InnerCalmOuterStrength(),
                new InnerCalmOuterStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives no boost when casting leaves no other cards in hand")
    void givesNoBoostWithEmptyRemainingHand() {
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(new InnerCalmOuterStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The hand-size boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.setHand(player1, List.of(new InnerCalmOuterStrength(), new InnerCalmOuterStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        harness.setHand(player1, List.of(new InnerCalmOuterStrength()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
