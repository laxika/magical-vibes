package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SlitheringShade.class)
class SlitheringShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while its controller has cards in hand")
    void cannotAttackWithCardsInHand() {
        harness.setHand(player1, List.of(new SlitheringShade()));
        Permanent shade = addCreatureReady(player1, new SlitheringShade());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(shade.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with an empty hand")
    void canAttackWithEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SlitheringShade()));
        Permanent shade = addCreatureReady(player1, new SlitheringShade());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(shade.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The black ability gives +1/+1 without tapping the creature")
    void abilityBoostsSelf() {
        Permanent shade = addCreatureReady(player1, new SlitheringShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(1);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent shade = addCreatureReady(player1, new SlitheringShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isZero();
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

}
