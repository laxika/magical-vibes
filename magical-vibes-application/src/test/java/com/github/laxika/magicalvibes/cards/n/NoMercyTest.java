package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GoblinFireslinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoMercyTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that deals noncombat damage to No Mercy's controller is destroyed")
    void noncombatDamageDestroysCreature() {
        harness.addToBattlefield(player2, new NoMercy());
        Permanent fireslinger = addCreatureReady(player1, new GoblinFireslinger());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fireslinger);
        harness.assertInGraveyard(player1, "Goblin Fireslinger");
    }

    @Test
    @DisplayName("A creature that deals combat damage to No Mercy's controller is destroyed")
    void combatDamageDestroysCreature() {
        harness.addToBattlefield(player2, new NoMercy());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
