package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

class MatsuTribeSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability damages a flyer, taps it, and locks its next untap step")
    void activatedAbilityTapsAndLocksDamagedFlyer() {
        addReady(player1, new MatsuTribeSniper());
        Permanent flyer = addReady(player2, new AngelOfMercy());

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isTrue();
        assertThat(flyer.getSkipUntapCount()).isEqualTo(1);
        assertThat(flyer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addReady(player1, new MatsuTribeSniper());
        Permanent creature = addReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Combat damage also taps and locks the damaged creature")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent sniper = addReady(player1, new MatsuTribeSniper());
        sniper.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveStack();

        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
