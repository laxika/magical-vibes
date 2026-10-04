package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireAtWill.class, GrizzlyBears.class, HillGiant.class})
class FireAtWillTest extends BaseCardTest {

    private Permanent addCombatant(com.github.laxika.magicalvibes.model.Player owner, com.github.laxika.magicalvibes.model.Card card, boolean attacking) {
        Permanent perm = harness.addToBattlefieldAndReturn(owner, card);
        perm.setSummoningSick(false);
        if (attacking) {
            perm.setAttacking(true);
        } else {
            perm.setBlocking(true);
        }
        return perm;
    }

    @Test
    void deals3DamageToSingleAttackingCreature() {
        Permanent attacker = addCombatant(player1, new HillGiant(), true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, Map.of(attacker.getId(), 3));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void dividesDamageAmongTwoAttackingCreatures() {
        Permanent attacker1 = addCombatant(player1, new HillGiant(), true);
        Permanent attacker2 = addCombatant(player1, new HillGiant(), true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, Map.of(attacker1.getId(), 2, attacker2.getId(), 1));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker1.getId()) && p.getMarkedDamage() == 2)
                .anyMatch(p -> p.getId().equals(attacker2.getId()) && p.getMarkedDamage() == 1);
    }

    @Test
    void canTargetBlockingCreature() {
        Permanent blocker = addCombatant(player1, new HillGiant(), false);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, Map.of(blocker.getId(), 3));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Fire at Will"));
    }

    @Test
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID nonCombatId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, Map.of(nonCombatId, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignmentsMustSumTo3() {
        Permanent attacker = addCombatant(player1, new HillGiant(), true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, Map.of(attacker.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dividesDamageAmongThreeAttackersAndBlockers() {
        Permanent attacker = addCombatant(player1, new HillGiant(), true);
        Permanent blocker1 = addCombatant(player2, new HillGiant(), false);
        Permanent blocker2 = addCombatant(player2, new HillGiant(), false);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, Map.of(attacker.getId(), 1, blocker1.getId(), 1, blocker2.getId(), 1));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker1.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotAssignZeroDamageToATarget() {
        Permanent attacker1 = addCombatant(player1, new HillGiant(), true);
        Permanent attacker2 = addCombatant(player1, new HillGiant(), true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                Map.of(attacker1.getId(), 3, attacker2.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDamageCreatureThatLeftCombatBeforeResolution() {
        Permanent attacker = addCombatant(player1, new HillGiant(), true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, Map.of(attacker.getId(), 3));

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Fire at Will");
    }

    @Test
    void doesNotRedistributeDamageWhenOneTargetLeavesCombat() {
        Permanent attacker = addCombatant(player1, new HillGiant(), true);
        Permanent blocker = addCombatant(player2, new HillGiant(), false);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FireAtWill()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, Map.of(attacker.getId(), 2, blocker.getId(), 1));

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }
}
