package com.github.laxika.magicalvibes.cards.t;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TacticalAdvantage.class, GrizzlyBears.class})
class TacticalAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature you control +2/+2")
    void boostsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player1);

        castAt(blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives a blocked creature you control +2/+2")
    void boostsBlockedCreature() {
        Permanent blocked = addBlockedCreature();

        castAt(blocked.getId());

        assertThat(blocked.getPowerModifier()).isEqualTo(2);
        assertThat(blocked.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent blocker = addBlockingCreature(player1);

        castAt(blocker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking or blocked")
    void cannotTargetNonCombatCreature() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking or blocked");
    }

    @Test
    @DisplayName("Cannot target an opponent's blocking creature")
    void cannotTargetCreatureOpponentControls() {
        Permanent blocker = addBlockingCreature(player2);
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent blocker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        blocker.setBlocking(true);
        return blocker;
    }

    private Permanent addBlockedCreature() {
        Permanent blocked = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocked.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());
        return blocked;
    }

    private void castAt(UUID targetId) {
        setupSpell();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void setupSpell() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TacticalAdvantage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
