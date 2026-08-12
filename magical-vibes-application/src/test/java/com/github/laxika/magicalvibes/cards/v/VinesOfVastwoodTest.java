package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VinesOfVastwoodTest extends BaseCardTest {

    @Test
    @DisplayName("The target cannot be targeted by an opponent, but its controller can target it")
    void protectsAgainstOpponentsOnly() {
        Permanent target = addCreature(player1);
        castVines(target, false);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Kicker gives the target +4/+4")
    void kickedBoostsTarget() {
        Permanent target = addCreature(player1);
        castVines(target, true);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The targeting restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        castVines(target, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
    }

    private void castVines(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new VinesOfVastwood()));
        harness.addMana(player1, ManaColor.GREEN, kicked ? 2 : 1);
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
