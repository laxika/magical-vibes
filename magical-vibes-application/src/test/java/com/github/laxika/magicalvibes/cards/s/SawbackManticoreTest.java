package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawbackManticore.class, ZhalfirinKnight.class})
class SawbackManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("{4} grants flying until end of turn")
    void grantsFlying() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, manticore, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, manticore, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{1} deals 2 damage to a blocking creature while attacking — kills a 2/2")
    void dealsTwoDamageWhileAttacking() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        manticore.setAttacking(true);
        blocker.setBlocking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("{1} is activatable while this creature is blocking")
    void activatableWhileBlocking() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());
        Permanent attacker = addCreatureReady(player2, new ZhalfirinKnight());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        manticore.setBlocking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("{1} cannot be activated while neither attacking nor blocking")
    void cannotActivateOutsideCombat() {
        addCreatureReady(player1, new SawbackManticore());
        Permanent other = addCreatureReady(player2, new ZhalfirinKnight());
        other.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = other.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("{1} can only be activated once each turn")
    void onlyOncePerTurn() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());
        Permanent otherBlocker = addCreatureReady(player2, new ZhalfirinKnight());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        manticore.setAttacking(true);
        blocker.setBlocking(true);
        otherBlocker.setBlocking(true);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 1, null, blocker.getId());

        UUID secondTargetId = otherBlocker.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, secondTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1} cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatant() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());
        Permanent bystander = addCreatureReady(player2, new ZhalfirinKnight());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        manticore.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = bystander.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1} cannot target a player")
    void cannotTargetPlayer() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());
        manticore.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1} does nothing if its target is no longer attacking or blocking")
    void targetMustStillBeAttackingOrBlockingAtResolution() {
        Permanent manticore = addCreatureReady(player1, new SawbackManticore());
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        manticore.setAttacking(true);
        target.setBlocking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Zhalfirin Knight");
    }
}
