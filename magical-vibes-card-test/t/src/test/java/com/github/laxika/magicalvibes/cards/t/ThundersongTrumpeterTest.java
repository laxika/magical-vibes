package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThundersongTrumpeter.class, Watchwolf.class, BorosSignet.class})
class ThundersongTrumpeterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it prevents the target creature from attacking or blocking this turn")
    void targetCannotAttackOrBlockThisTurn() {
        Permanent trumpeter = addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player2, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(trumpeter.isTapped()).isTrue();
        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The ability does not affect other creatures")
    void otherCreaturesAreUnaffected() {
        addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player2, new Watchwolf());
        Permanent other = addCreatureReady(player2, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, other, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, other)).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature its controller controls")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player1, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, target, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent trumpeter = addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(trumpeter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can target the Trumpeter itself")
    void canTargetItself() {
        Permanent trumpeter = addCreatureReady(player1, new ThundersongTrumpeter());

        harness.activateAbility(player1, 0, null, trumpeter.getId());
        harness.passBothPriorities();
        trumpeter.untap();

        assertThat(als.canAttack(gd, trumpeter, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, trumpeter)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Trumpeter cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent trumpeter = harness.addToBattlefieldAndReturn(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player2, new Watchwolf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(trumpeter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(als.canAttack(gd, target, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("The ability resolves even if its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent trumpeter = addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player2, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trumpeter);
        gd.playerGraveyards.get(player1.getId()).add(trumpeter.getCard());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The restrictions expire at end of turn")
    void restrictionsExpireAtEndOfTurn() {
        addCreatureReady(player1, new ThundersongTrumpeter());
        Permanent target = addCreatureReady(player2, new Watchwolf());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(als.canAttack(gd, target, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }
}
