package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StampedingRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArbalestElite.class, RuneclawBear.class, MindControl.class, StampedingRhino.class})
class ArbalestEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to an attacking creature, killing a 2/2")
    void damagesAttackingCreature() {
        addReadyElite(player1);
        addMana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setAttacking(true);
        UUID bearsId = bear.getId();

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Activating taps the Elite and keeps it from untapping next untap step")
    void activationExertsSelf() {
        Permanent elite = addReadyElite(player1);
        addMana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setBlocking(true);
        UUID bearsId = bear.getId();

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(elite.isTapped()).isTrue();
        assertThat(elite.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addReadyElite(player1);
        addMana(player1);
        harness.addToBattlefield(player2, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void skipsOnlyTheNextUntapStep() {
        Permanent elite = addReadyElite(player1);
        addMana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setAttacking(true);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(elite.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(elite.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(elite.isTapped()).isFalse();
    }

    @Test
    void illegalTargetPreventsDamageAndUntapRestriction() {
        Permanent elite = addReadyElite(player1);
        addMana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setAttacking(true);

        harness.activateAbility(player1, 0, null, bear.getId());
        bear.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(elite.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(elite.isTapped()).isFalse();
    }

    @Test
    void canKillAnOwnBlockingCreature() {
        addReadyElite(player1);
        addMana(player1);
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new ArbalestElite());
        blocker.setBlocking(true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Arbalest Elite");
    }

    @Test
    void changingControllerDoesNotRestrictTheNewControllersUntapStep() {
        Permanent elite = addReadyElite(player1);
        addMana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setAttacking(true);
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MindControl()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0, elite.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elite);

        harness.performUntapStep(player2);
        assertThat(elite.isTapped()).isFalse();
    }

    @Test
    void threeDamageDoesNotKillAFourToughnessAttacker() {
        addReadyElite(player1);
        addMana(player1);
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new StampedingRhino());
        rhino.setAttacking(true);

        harness.activateAbility(player1, 0, null, rhino.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Stampeding Rhino");
        assertThat(rhino.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ArbalestElite());
        addMana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyElite(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ArbalestElite());
        perm.setSummoningSick(false);
        return perm;
    }
}
