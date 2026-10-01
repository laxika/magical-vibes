package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MorselTheft;
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

@CardUsed({VengefulFirebrand.class, MorselTheft.class})
class VengefulFirebrandTest extends BaseCardTest {

    // ===== Conditional haste: Warrior card in graveyard =====

    @Test
    @DisplayName("No haste with empty graveyard")
    void noHasteWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new VengefulFirebrand());

        assertThat(gqs.hasKeyword(gd, findFirebrand(), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("No haste with only a non-Warrior card in graveyard")
    void noHasteWithNonWarrior() {
        harness.setGraveyard(player1, List.of(new MorselTheft()));
        harness.addToBattlefield(player1, new VengefulFirebrand());

        assertThat(gqs.hasKeyword(gd, findFirebrand(), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Has haste with a Warrior card in graveyard")
    void hasteWithWarriorInGraveyard() {
        // Vengeful Firebrand is itself an Elemental Warrior.
        harness.setGraveyard(player1, List.of(new VengefulFirebrand()));
        harness.addToBattlefield(player1, new VengefulFirebrand());

        assertThat(gqs.hasKeyword(gd, findFirebrand(), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Only the controller's graveyard counts")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new VengefulFirebrand()));
        harness.addToBattlefield(player1, new VengefulFirebrand());

        assertThat(gqs.hasKeyword(gd, findFirebrand(), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste when the Warrior card leaves the graveyard")
    void losesHasteWhenGraveyardChanges() {
        harness.setGraveyard(player1, List.of(new VengefulFirebrand()));
        harness.addToBattlefield(player1, new VengefulFirebrand());
        assertThat(gqs.hasKeyword(gd, findFirebrand(), Keyword.HASTE)).isTrue();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, findFirebrand(), Keyword.HASTE)).isFalse();
    }

    // ===== Firebreathing: {R}: +1/+0 until end of turn =====

    @Test
    @DisplayName("{R} gives +1/+0")
    void firebreathingBoosts() {
        Permanent firebrand = harness.addToBattlefieldAndReturn(player1, new VengefulFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Firebreathing stacks with repeated activations")
    void firebreathingStacks() {
        Permanent firebrand = harness.addToBattlefieldAndReturn(player1, new VengefulFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);

        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("Firebreathing requires red mana")
    void firebreathingRequiresRedMana() {
        harness.addToBattlefield(player1, new VengefulFirebrand());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Firebreathing only boosts power and wears off at end of turn")
    void firebreathingExpiresAtEndOfTurn() {
        Permanent firebrand = harness.addToBattlefieldAndReturn(player1, new VengefulFirebrand());
        int basePower = gqs.getEffectivePower(gd, firebrand);
        int baseToughness = gqs.getEffectiveToughness(gd, firebrand);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, firebrand)).isEqualTo(baseToughness);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firebrand)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, firebrand)).isEqualTo(baseToughness);
    }

    private Permanent findFirebrand() {
        return findPermanent(player1, "Vengeful Firebrand");
    }
}
