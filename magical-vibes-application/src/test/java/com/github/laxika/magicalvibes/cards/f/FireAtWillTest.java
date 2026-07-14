package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireAtWillTest extends BaseCardTest {

    private Permanent addCombatant(com.github.laxika.magicalvibes.model.Player owner, com.github.laxika.magicalvibes.model.Card card, boolean attacking) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        if (attacking) {
            perm.setAttacking(true);
        } else {
            perm.setBlocking(true);
        }
        harness.getGameData().playerBattlefields.get(owner.getId()).add(perm);
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
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
        Permanent attacker = addCombatant(player1, new GrizzlyBears(), true);

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
}
