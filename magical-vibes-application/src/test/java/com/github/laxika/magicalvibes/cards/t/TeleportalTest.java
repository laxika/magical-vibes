package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeleportalTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gets +1/+0 and can't be blocked")
    void targetsOwnCreature() {
        Permanent target = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent target = addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload boosts and makes every creature you control unblockable")
    void overloadAffectsAllOwnCreatures() {
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        addCreatureReady(player2);
        harness.setHand(player1, List.of(new Teleportal()));
        addOverloadMana();

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);

        first.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by no creatures");
    }

    @Test
    @DisplayName("The temporary effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Overload requires its alternate cost")
    void overloadRequiresFullCost() {
        addCreatureReady(player1);
        harness.setHand(player1, List.of(new Teleportal()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addOverloadMana() {
        addNormalMana();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
