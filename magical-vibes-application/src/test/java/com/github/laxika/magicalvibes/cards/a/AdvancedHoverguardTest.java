package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MagmaJet;
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

@CardUsed({AdvancedHoverguard.class, MagmaJet.class})
class AdvancedHoverguardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants shroud until end of turn")
    void resolvingAbilityGrantsShroud() {
        Permanent hoverguard = addCreatureReady(player1, new AdvancedHoverguard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hoverguard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOff() {
        Permanent hoverguard = addCreatureReady(player1, new AdvancedHoverguard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hoverguard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one blue mana")
    void requiresBlueMana() {
        addCreatureReady(player1, new AdvancedHoverguard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Shroud prevents this creature from being targeted by a spell")
    void shroudPreventsTargetedSpell() {
        Permanent hoverguard = addCreatureReady(player1, new AdvancedHoverguard());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new MagmaJet()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hoverguard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud does not prevent activating this creature's own ability")
    void shroudDoesNotPreventOwnAbility() {
        Permanent hoverguard = addCreatureReady(player1, new AdvancedHoverguard());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hoverguard, Keyword.SHROUD)).isTrue();
    }
}
