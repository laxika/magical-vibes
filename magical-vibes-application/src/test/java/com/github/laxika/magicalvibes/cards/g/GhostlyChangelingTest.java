package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlyChangeling.class, ImperiousPerfect.class})
class GhostlyChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {1}{B} gives +1/+1")
    void activatingGivesBoost() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent changeling = findPermanent(player1, "Ghostly Changeling");
        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(changeling.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly to stack the boost")
    void boostStacks() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent changeling = findPermanent(player1, "Ghostly Changeling");
        assertThat(changeling.getPowerModifier()).isEqualTo(2);
        assertThat(changeling.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability requires mana to activate")
    void abilityRequiresMana() {
        addChangelingReady(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent changeling = findPermanent(player1, "Ghostly Changeling");
        assertThat(changeling.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(0);
        assertThat(changeling.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Changeling receives the bonus for other Elves you control")
    void receivesElfTribalBonus() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new GhostlyChangeling());
        harness.addToBattlefield(player1, new ImperiousPerfect());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new GhostlyChangeling());
        changeling.setSummoningSick(true);
        changeling.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(changeling.getPowerModifier()).isZero();
        assertThat(changeling.getToughnessModifier()).isZero();
        harness.passBothPriorities();

        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(changeling.getToughnessModifier()).isEqualTo(1);
        assertThat(changeling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the black requirement")
    void requiresBlackMana() {
        addChangelingReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChangelingReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GhostlyChangeling());
        perm.setSummoningSick(false);
        return perm;
    }
}
