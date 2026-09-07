package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlitheringShade.class, GrizzlyBears.class})
class SlitheringShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while its controller has cards in hand")
    void cannotAttackWithCardsInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent shade = addShadeReady(player1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(shade.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack with an empty hand")
    void canAttackWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent shade = addShadeReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(shade.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The black ability gives +1/+1 without tapping the creature")
    void abilityBoostsSelf() {
        Permanent shade = addShadeReady(player1);
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
        Permanent shade = addShadeReady(player1);
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

    private Permanent addShadeReady(Player player) {
        Permanent perm = new Permanent(new SlitheringShade());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
