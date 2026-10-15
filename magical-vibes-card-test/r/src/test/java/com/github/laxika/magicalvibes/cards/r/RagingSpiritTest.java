package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed(RagingSpirit.class)
class RagingSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {2} makes it colorless until end of turn")
    void activatingMakesItColorless() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, spirit)).isEmpty();
    }

    @Test
    @DisplayName("The colorless setting wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<CardColor> originalColors = List.copyOf(gqs.getEffectiveColors(gd, spirit));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, spirit)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactlyElementsOf(originalColors);
    }

    @Test
    @DisplayName("Cannot activate without paying the {2} cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new RagingSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability changes only its source and only when it resolves")
    void onlySourceBecomesColorlessOnResolution() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        Permanent friendlySpirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        Permanent opposingSpirit = harness.addToBattlefieldAndReturn(player2, new RagingSpirit());
        List<CardColor> originalColors = List.copyOf(gqs.getEffectiveColors(gd, spirit));
        List<CardColor> friendlyColors = List.copyOf(gqs.getEffectiveColors(gd, friendlySpirit));
        List<CardColor> opposingColors = List.copyOf(gqs.getEffectiveColors(gd, opposingSpirit));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactlyElementsOf(originalColors);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, spirit)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, friendlySpirit)).containsExactlyElementsOf(friendlyColors);
        assertThat(gqs.getEffectiveColors(gd, opposingSpirit)).containsExactlyElementsOf(opposingColors);
    }

    @Test
    @DisplayName("A tapped creature can activate using colored mana for the generic cost")
    void tappedCreatureCanActivateWithColoredMana() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        spirit.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, spirit)).isEmpty();
        assertThat(spirit.isTapped()).isTrue();
    }
}
