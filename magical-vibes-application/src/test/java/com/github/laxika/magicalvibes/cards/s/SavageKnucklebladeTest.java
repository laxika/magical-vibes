package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavageKnucklebladeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the green ability gives +2/+2 until end of turn")
    void greenAbilityBoostsSelf() {
        Permanent knuckleblade = addReadyKnuckleblade(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(knuckleblade.getEffectivePower()).isEqualTo(6);
        assertThat(knuckleblade.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The green ability can be activated only once each turn")
    void greenAbilityIsLimitedToOncePerTurn() {
        addReadyKnuckleblade(player1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The green ability wears off at end of turn")
    void greenAbilityResetsAtEndOfTurn() {
        Permanent knuckleblade = addReadyKnuckleblade(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knuckleblade.getEffectivePower()).isEqualTo(4);
        assertThat(knuckleblade.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolving the blue ability returns the creature to its owner's hand")
    void blueAbilityReturnsSelfToHand() {
        addReadyKnuckleblade(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Savage Knuckleblade");
        harness.assertInHand(player1, "Savage Knuckleblade");
    }

    @Test
    @DisplayName("Resolving the red ability grants haste until end of turn")
    void redAbilityGrantsHaste() {
        Permanent knuckleblade = addReadyKnuckleblade(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(knuckleblade.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knuckleblade.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private Permanent addReadyKnuckleblade(Player player) {
        Permanent permanent = new Permanent(new SavageKnuckleblade());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
