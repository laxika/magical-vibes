package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravelSlinger.class, GlorySeeker.class})
class GravelSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to an attacking creature")
    void dealsDamageToAttackingCreature() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(slinger.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        Permanent blocker = addCombatCreature(player2, false);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(slinger.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an attacking creature its controller controls")
    void dealsDamageToOwnAttackingCreature() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        Permanent attacker = addCombatCreature(player1, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(slinger.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new GravelSlinger());
        Permanent bystander = addCreatureReady(player2, new GlorySeeker());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    void canBeTurnedFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new GravelSlinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent slinger = findPermanent(player1, "Gravel Slinger");
        assertThat(slinger.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(slinger));
        harness.passBothPriorities();

        assertThat(slinger.isFaceDown()).isFalse();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        slinger.tap();
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        slinger.setSummoningSick(true);
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(slinger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDamageTargetThatLeavesCombatBeforeResolution() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(slinger.isTapped()).isTrue();
    }

    @Test
    void faceDownSlingerHasNoDamageAbility() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        slinger.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(slinger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateImmediatelyAfterTurningFaceUpDuringCombat() {
        Permanent slinger = addCreatureReady(player1, new GravelSlinger());
        slinger.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.turnFaceUp(player1, 0);
        assertThat(slinger.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(slinger.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addCombatCreature(Player player, boolean attacking) {
        Permanent creature = addCreatureReady(player, new GlorySeeker());
        if (attacking) {
            creature.setAttacking(true);
        } else {
            creature.setBlocking(true);
        }
        return creature;
    }
}
