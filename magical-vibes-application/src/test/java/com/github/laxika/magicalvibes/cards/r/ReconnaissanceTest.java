package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reconnaissance.class, RagingGoblin.class})
class ReconnaissanceTest extends BaseCardTest {

    @Test
    void removesAndUntapsAnAttackingCreatureYouControl() {
        harness.addToBattlefield(player1, new Reconnaissance());
        Permanent attacker = addAttacker(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    void cannotTargetAnAttackingCreatureAnOpponentControls() {
        harness.addToBattlefield(player1, new Reconnaissance());
        Permanent attacker = addAttacker(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAControlledCreatureThatIsNotAttacking() {
        harness.addToBattlefield(player1, new Reconnaissance());
        Permanent creature = addCreatureReady(player1, new RagingGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyRemovesAndUntapsTheChosenAttacker() {
        harness.addToBattlefield(player1, new Reconnaissance());
        Permanent chosenAttacker = addAttacker(player1);
        Permanent otherAttacker = addAttacker(player1);

        harness.activateAbility(player1, 0, null, chosenAttacker.getId());
        harness.passBothPriorities();

        assertThat(chosenAttacker.isAttacking()).isFalse();
        assertThat(chosenAttacker.isTapped()).isFalse();
        assertThat(otherAttacker.isAttacking()).isTrue();
        assertThat(otherAttacker.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(otherAttacker.isTapped()).isTrue();
    }

    @Test
    void doesNotResolveIfTheTargetStopsAttackingBeforeResolution() {
        harness.addToBattlefield(player1, new Reconnaissance());
        Permanent attacker = addAttacker(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    void canBeActivatedAfterCombatDamageDuringEndOfCombat() {
        harness.addToBattlefield(player1, new Reconnaissance());
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.assertLife(player2, 19);
        assertThat(attacker.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new RagingGoblin());
        attacker.setAttacking(true);
        attacker.setAttackTarget(controller.equals(player1) ? player2.getId() : player1.getId());
        attacker.tap();
        return attacker;
    }
}
