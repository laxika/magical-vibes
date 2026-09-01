package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamPuff.class, GrizzlyBears.class})
class ScreamPuffTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player creates a Food token")
    void combatDamageCreatesFood() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Combat damage that is dealt to a blocker does not create a Food token")
    void combatDamageToCreatureDoesNotCreateFood() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("The Food token can be sacrificed to gain 3 life")
    void foodCanBeSacrificedForLife() {
        Permanent screamPuff = addCreatureReady(player1, new ScreamPuff());
        screamPuff.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }
}
