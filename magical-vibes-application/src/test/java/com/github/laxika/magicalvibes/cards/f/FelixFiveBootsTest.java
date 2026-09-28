package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HoardRobber;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelixFiveBoots.class, HoardRobber.class, GrizzlyBears.class})
class FelixFiveBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Felix doubles triggers caused by a creature dealing combat damage to a player")
    void doublesCombatDamageTriggers() {
        addCreatureReady(player1, new FelixFiveBoots());
        Permanent robber = addCreatureReady(player1, new HoardRobber());
        robber.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Felix does not double a combat-damage trigger from an opponent's creature")
    void doesNotDoubleOpponentCombatDamageTrigger() {
        addCreatureReady(player1, new FelixFiveBoots());
        Permanent robber = addCreatureReady(player2, new HoardRobber());
        robber.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }
}
