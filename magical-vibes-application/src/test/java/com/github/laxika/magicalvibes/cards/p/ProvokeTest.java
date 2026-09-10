package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Provoke.class, SkyshroudFalcon.class, SpinedWurm.class})
class ProvokeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps an opponent's creature and draws a card")
    void untapsOpponentCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());
        target.tap();
        castProvoke(target);

        assertThat(target.isTapped()).isFalse();
        harness.assertInHand(player1, "Spined Wurm");
    }

    @Test
    @DisplayName("The targeted creature must block this turn if able")
    void targetMustBlockIfAble() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        castProvoke(target);

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(target.isBlocking()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not require a block when the targeted creature cannot block any attacker")
    void doesNotRequireBlockWhenUnable() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SkyshroudFalcon());
        castProvoke(target);

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(target.isBlocking()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SpinedWurm());
        harness.setHand(player1, List.of(new Provoke()));
        addManaForProvoke();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castProvoke(Permanent target) {
        harness.setHand(player1, List.of(new Provoke()));
        harness.setLibrary(player1, List.of(new SpinedWurm()));
        addManaForProvoke();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addManaForProvoke() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
