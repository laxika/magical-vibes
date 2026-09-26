package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HornOfDeafening;
import com.github.laxika.magicalvibes.cards.w.WallOfHeat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Transmutation.class, WallOfHeat.class, HornOfDeafening.class})
class TransmutationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Transmutation switches a target creature's power and toughness")
    void switchesPowerAndToughness() {
        harness.addToBattlefield(player1, new WallOfHeat());
        Permanent wall = findPermanent(player1, "Wall of Heat");
        harness.setHand(player1, List.of(new Transmutation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID wallId = harness.getPermanentId(player1, "Wall of Heat");
        harness.castInstant(player1, 0, wallId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
    }

    @Test
    @DisplayName("Transmutation's switch wears off at cleanup")
    void switchWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new WallOfHeat());
        Permanent wall = findPermanent(player1, "Wall of Heat");
        harness.setHand(player1, List.of(new Transmutation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID wallId = harness.getPermanentId(player1, "Wall of Heat");
        harness.castInstant(player1, 0, wallId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(6);
    }

    @Test
    @DisplayName("Transmutation cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new HornOfDeafening());
        harness.setHand(player1, List.of(new Transmutation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Horn of Deafening");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Transmutation can target a creature controlled by an opponent")
    void switchesOpponentCreature() {
        harness.addToBattlefield(player2, new WallOfHeat());
        Permanent wall = findPermanent(player2, "Wall of Heat");
        harness.setHand(player1, List.of(new Transmutation()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID wallId = harness.getPermanentId(player2, "Wall of Heat");
        harness.castInstant(player1, 0, wallId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
    }
}
