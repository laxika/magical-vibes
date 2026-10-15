package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdultGoldDragon.class, DireWolfProwler.class})
class AdultGoldDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents non-flying creatures from blocking Adult Gold Dragon")
    void flyingPreventsGroundBlockers() {
        Permanent dragon = addCreatureReady(player1, new AdultGoldDragon());
        Permanent blocker = addCreatureReady(player2, new DireWolfProwler());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dragon);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Haste allows Adult Gold Dragon to attack immediately")
    void hasteAllowsImmediateAttack() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new AdultGoldDragon());
        harness.addToBattlefield(player2, new AdultGoldDragon());

        declareAttackers(List.of(0));

        assertThat(dragon.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Lifelink gains life when Adult Gold Dragon deals combat damage")
    void lifelinkGainsLifeOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AdultGoldDragon());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Flying creatures can block the Dragon and both gain life before dying")
    void lifelinkGainsLifeEvenWhenDragonDiesInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AdultGoldDragon());
        addCreatureReady(player2, new AdultGoldDragon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 24);
        harness.assertInGraveyard(player1, "Adult Gold Dragon");
        harness.assertInGraveyard(player2, "Adult Gold Dragon");
        harness.assertNotOnBattlefield(player1, "Adult Gold Dragon");
        harness.assertNotOnBattlefield(player2, "Adult Gold Dragon");
    }

    @Test
    @DisplayName("Blocking gains life for the Dragon's full damage, including excess lethal damage")
    void lifelinkGainsFullDamageWhenBlockingSmallerCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DireWolfProwler());
        harness.addToBattlefield(player2, new AdultGoldDragon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
        harness.assertInGraveyard(player1, "Dire Wolf Prowler");
        harness.assertNotOnBattlefield(player1, "Dire Wolf Prowler");
        harness.assertOnBattlefield(player2, "Adult Gold Dragon");
    }
}
