package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineArrow.class, PrimordialWurm.class, GoblinAssailant.class})
class DivineArrowTest extends BaseCardTest {

    @Test
    void dealsFourDamageToAnAttackingCreature() {
        Permanent target = addCreatureReady(player1, new PrimordialWurm());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineArrow()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Primordial Wurm")
                        && permanent.getMarkedDamage() == 4);
    }

    @Test
    void dealsFourDamageToABlockingCreature() {
        Permanent target = addCreatureReady(player2, new GoblinAssailant());
        target.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DivineArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Goblin Assailant");
        harness.assertInGraveyard(player2, "Goblin Assailant");
    }

    @Test
    void cannotTargetANonCombatCreature() {
        harness.addToBattlefield(player1, new PrimordialWurm());
        UUID targetId = harness.getPermanentId(player1, "Primordial Wurm");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineArrow()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    void canTargetItsControllersAttackingCreature() {
        Permanent target = addCreatureReady(player1, new GoblinAssailant());
        target.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new DivineArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Assailant");
        harness.assertInGraveyard(player1, "Goblin Assailant");
        harness.assertInGraveyard(player1, "Divine Arrow");
    }

    @Test
    void dealsNoDamageIfTargetStopsAttackingBeforeResolution() {
        Permanent target = addCreatureReady(player1, new GoblinAssailant());
        target.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new DivineArrow()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Assailant");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Divine Arrow");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsNoDamageIfTargetStopsBlockingBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GoblinAssailant());
        target.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new DivineArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Assailant");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Divine Arrow");
        assertThat(gd.stack).isEmpty();
    }
}
