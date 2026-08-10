package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FurnaceBroodTest extends BaseCardTest {

    @Test
    @DisplayName("The red ability marks a target creature so it can't be regenerated this turn")
    void marksTargetCreature() {
        addReadyFurnaceBrood(player1);
        Permanent skeleton = addRegeneratingSkeleton(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, skeleton.getId());
        harness.passBothPriorities();

        assertThat(skeleton.isCantRegenerateThisTurn()).isTrue();
        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration-prevention mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        addReadyFurnaceBrood(player1);
        Permanent skeleton = addRegeneratingSkeleton(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, skeleton.getId());
        harness.passBothPriorities();
        assertThat(skeleton.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skeleton.isCantRegenerateThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyFurnaceBrood(player1);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyFurnaceBrood(Player player) {
        Permanent permanent = new Permanent(new FurnaceBrood());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addRegeneratingSkeleton(Player player) {
        Permanent permanent = new Permanent(new DrudgeSkeletons());
        permanent.setSummoningSick(false);
        permanent.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
