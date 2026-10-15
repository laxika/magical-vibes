package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(AncientKavu.class)
class AncientKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {2} makes it colorless until end of turn")
    void activatingMakesItColorless() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, kavu)).isEmpty();
    }

    @Test
    @DisplayName("The colorless setting wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<CardColor> originalColors = List.copyOf(gqs.getEffectiveColors(gd, kavu));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, kavu)).isEmpty();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactlyElementsOf(originalColors);
    }

    @Test
    @DisplayName("Cannot activate without paying the {2} cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new AncientKavu());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The color change waits for resolution and affects only its source")
    void changesOnlyItsSourceOnResolution() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AncientKavu());
        List<CardColor> originalColors = List.copyOf(gqs.getEffectiveColors(gd, kavu));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactlyElementsOf(originalColors);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, kavu)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, other)).containsExactlyElementsOf(originalColors);
        assertThat(gqs.getEffectiveColors(gd, opponent)).containsExactlyElementsOf(originalColors);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Kavu can activate repeatedly")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        kavu.tap();
        kavu.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, kavu)).isEmpty();
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, kavu)).isEmpty();
        assertThat(kavu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One mana is insufficient to activate")
    void cannotActivateWithOnlyOneMana() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new AncientKavu());
        List<CardColor> originalColors = List.copyOf(gqs.getEffectiveColors(gd, kavu));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactlyElementsOf(originalColors);
    }
}
