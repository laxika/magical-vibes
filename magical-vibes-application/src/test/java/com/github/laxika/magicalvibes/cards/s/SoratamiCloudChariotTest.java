package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoratamiCloudChariotTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        addReadyChariot(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The second ability prevents combat damage to and by the target creature")
    void preventsCombatDamageToAndByTarget() {
        harness.setLife(player2, 20);
        addReadyChariot(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("Both abilities can target only a creature you control")
    void cannotTargetOpponentCreature() {
        addReadyChariot(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChariot(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SoratamiCloudChariot());
    }
}
