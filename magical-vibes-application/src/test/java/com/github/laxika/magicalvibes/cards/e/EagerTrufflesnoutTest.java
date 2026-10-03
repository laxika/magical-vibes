package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TreetopSnarespinner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EagerTrufflesnout.class, TreetopSnarespinner.class, LlanowarElves.class})
class EagerTrufflesnoutTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it deals combat damage to a player")
    void createsFoodTokenOnCombatDamageToPlayer() {
        addAttackingTrufflesnout(player1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player1, "Food");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Does not create a Food token when the blocker absorbs all combat damage")
    void doesNotCreateFoodTokenWhenBlocked() {
        addAttackingTrufflesnout(player1);
        Permanent blocker = addCreatureReady(player2, new TreetopSnarespinner());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Trampling over a blocker creates one Food token")
    void createsFoodWhenTramplingOverBlocker() {
        addAttackingTrufflesnout(player1);
        Permanent blocker = addCreatureReady(player2, new LlanowarElves());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and gains life only when its ability resolves")
    void foodCanBeSacrificedForThreeLife() {
        addAttackingTrufflesnout(player1);
        resolveCombat();
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void addAttackingTrufflesnout(Player player) {
        Permanent attacker = addCreatureReady(player, new EagerTrufflesnout());
        attacker.setAttacking(true);
    }
}
