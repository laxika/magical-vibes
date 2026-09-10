package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.p.Pyromancy;
import com.github.laxika.magicalvibes.cards.t.ThornwindFaeries;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoMercy.class, ThornwindFaeries.class, GiantCockroach.class, Pyromancy.class})
class NoMercyTest extends BaseCardTest {

    @Test
    @DisplayName("A creature that deals noncombat damage to No Mercy's controller is destroyed")
    void noncombatDamageDestroysCreature() {
        harness.addToBattlefield(player2, new NoMercy());
        Permanent thornwindFaeries = addCreatureReady(player1, new ThornwindFaeries());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(thornwindFaeries);
        harness.assertInGraveyard(player1, "Thornwind Faeries");
    }

    @Test
    @DisplayName("A creature that deals combat damage to No Mercy's controller is destroyed")
    void combatDamageDestroysCreature() {
        harness.addToBattlefield(player2, new NoMercy());
        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("No Mercy waits for its triggered ability to resolve before destroying the source")
    void sourceRemainsOnBattlefieldUntilTriggerResolves() {
        harness.addToBattlefield(player2, new NoMercy());
        Permanent attacker = addCreatureReady(player1, new ThornwindFaeries());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Thornwind Faeries");
    }

    @Test
    @DisplayName("Damage from a noncreature permanent does not destroy that permanent")
    void noncreatureDamageDoesNotDestroySource() {
        harness.addToBattlefield(player2, new NoMercy());
        harness.addToBattlefield(player1, new Pyromancy());
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player1, "Pyromancy");
        harness.assertInGraveyard(player1, "Giant Cockroach");
    }
}
