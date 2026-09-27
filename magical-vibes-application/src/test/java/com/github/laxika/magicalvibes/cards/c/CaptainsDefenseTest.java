package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({CaptainsDefense.class, Forest.class, GrizzlyBears.class})
class CaptainsDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a blocking creature and draws a card")
    void boostsBlockingCreatureAndDraws() {
        Permanent blocker = addBlockingCreature(player2);
        harness.setLibrary(player1, List.of(new Forest()));

        castAt(blocker);

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent blocker = addBlockingCreature(player1);
        harness.setLibrary(player1, List.of(new Forest()));

        castAt(blocker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addBlockingCreature(player1);
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent blocker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        blocker.setBlocking(true);
        return blocker;
    }

    private void castAt(Permanent target) {
        setupSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void setupSpell() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CaptainsDefense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
