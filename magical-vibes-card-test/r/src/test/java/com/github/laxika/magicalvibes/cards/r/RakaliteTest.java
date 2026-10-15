package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rakalite.class, GrizzlyBears.class, RodOfRuin.class})
class RakaliteTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next damage to a targeted player")
    void preventsNextDamageToTargetPlayer() {
        Permanent rakalite = addReadyRakalite(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rakalite);
    }

    @Test
    @DisplayName("Returns itself at the next end step after resolving the ability")
    void returnsItselfAtNextEndStep() {
        Permanent rakalite = addReadyRakalite(player1);
        Card card = rakalite.getCard();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rakalite);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    private Permanent addReadyRakalite(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Rakalite());
    }

    @Test
    void preventsOnlyOneDamageToCreature() {
        addReadyRakalite(player1);
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addToBattlefield(player1, new RodOfRuin());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isZero();

        harness.activateAbility(player1, 2, null, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void repeatedActivationsAccumulatePreventionWithoutTapping() {
        Permanent rakalite = addReadyRakalite(player1);
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();
        }
        for (int i = 1; i <= 2; i++) {
            harness.activateAbility(player1, i, null, player2.getId());
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 20);
        assertThat(rakalite.isTapped()).isFalse();
    }

    @Test
    void delayedReturnRemainsControlledByAbilityControllerAfterControlChanges() {
        Permanent rakalite = addReadyRakalite(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(rakalite);
        gd.playerBattlefields.get(player2.getId()).add(rakalite);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rakalite);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Rakalite");
    }

    @Test
    void illegalTargetPreventsSchedulingReturn() {
        Permanent rakalite = addReadyRakalite(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, creature);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rakalite);
    }
}
