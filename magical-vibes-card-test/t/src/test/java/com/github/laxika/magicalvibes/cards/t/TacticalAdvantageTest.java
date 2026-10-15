package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TacticalAdvantage.class, GrizzlyBears.class})
class TacticalAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature you control +2/+2")
    void boostsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player1);

        castAt(blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives a blocked creature you control +2/+2")
    void boostsBlockedCreature() {
        Permanent blocked = addBlockedCreature();

        castAt(blocked.getId());

        assertThat(blocked.getPowerModifier()).isEqualTo(2);
        assertThat(blocked.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent blocker = addBlockingCreature(player1);

        castAt(blocker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking or blocked")
    void cannotTargetNonCombatCreature() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking or blocked");
    }

    @Test
    @DisplayName("Cannot target an opponent's blocking creature")
    void cannotTargetCreatureOpponentControls() {
        Permanent blocker = addBlockingCreature(player2);
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot target an attacking creature that was not blocked")
    void cannotTargetUnblockedAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        setupSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking or blocked");
    }

    @Test
    @DisplayName("A blocked attacker remains eligible after its last blocker dies")
    void boostsBlockedAttackerAfterLastBlockerDies() {
        Permanent attacker = addBlockedCreature();
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        castAt(attacker.getId());

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost still resolves when the last blocker dies in response")
    void resolvesAfterLastBlockerDies() {
        Permanent attacker = addBlockedCreature();
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        setupSpell();
        harness.castInstant(player1, 0, attacker.getId());

        blocker.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a target that stops blocking before resolution")
    void doesNotBoostTargetRemovedFromCombat() {
        Permanent blocker = addBlockingCreature(player1);
        setupSpell();
        harness.castInstant(player1, 0, blocker.getId());

        blocker.setBlocking(false);
        blocker.getBlockingTargetIds().clear();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tactical Advantage");
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent blocker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        blocker.setBlocking(true);
        return blocker;
    }

    private Permanent addBlockedCreature() {
        Permanent blocked = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocked.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());
        return blocked;
    }

    private void castAt(UUID targetId) {
        setupSpell();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void setupSpell() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TacticalAdvantage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
