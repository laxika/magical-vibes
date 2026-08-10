package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = findPermanent(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player controller) {
        harness.addToBattlefield(controller, new GrizzlyBears());
        Permanent attacker = findPermanent(controller, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.tap();
        return attacker;
    }
}
