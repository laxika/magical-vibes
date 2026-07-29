package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes a creature with power 2 or less unblockable")
    void makesLowPowerCreatureUnblockable() {
        addReady(new DwarvenNomad(), player1);
        Permanent target = addReady(new GrizzlyBears(), player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps the Nomad")
    void activatingTapsSelf() {
        Permanent nomad = addReady(new DwarvenNomad(), player1);
        Permanent target = addReady(new GrizzlyBears(), player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(nomad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with power 3 is an illegal target")
    void cannotTargetHighPowerCreature() {
        addReady(new DwarvenNomad(), player1);
        Permanent giant = addReady(new HillGiant(), player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addReady(new DwarvenNomad(), player1);
        Permanent target = addReady(new GrizzlyBears(), player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    private Permanent addReady(Card card, Player player) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
