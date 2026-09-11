package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ShellShield.class, GrizzlyBears.class, GiantGrowth.class})
class ShellShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, gives a creature +0/+3")
    void withoutKickerBoostsTarget() {
        Permanent target = addCreature(player1);

        castResolve(target, false);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("With kicker, also gives the creature hexproof")
    void withKickerGrantsHexproof() {
        Permanent target = addCreature(player1);

        castResolve(target, true);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and hexproof expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addCreature(player1);

        castResolve(target, true);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castResolve(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, kicked ? 2 : 1);
        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
