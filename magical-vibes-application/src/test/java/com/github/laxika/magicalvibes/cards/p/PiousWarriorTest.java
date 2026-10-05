package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.cards.t.Thunderclap;
import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiousWarrior.class, ShockTroops.class, WildJhovall.class, Thunderclap.class})
class PiousWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to Pious Warrior")
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player2, new PiousWarrior());
        addCreatureReady(player1, new ShockTroops());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Pious Warrior");
    }

    @Test
    @DisplayName("Combat damage trigger resolves even if lethal damage destroys Pious Warrior")
    void gainsLifeWhenLethalCombatDamageDestroysIt() {
        Permanent warrior = addCreatureReady(player2, new PiousWarrior());
        addCreatureReady(player1, new WildJhovall());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Pious Warrior");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(warrior);
    }

    @Test
    @DisplayName("Attacking Pious Warrior gains life from damage received, not damage dealt")
    void gainsLifeWhenBlocked() {
        addCreatureReady(player1, new PiousWarrior());
        addCreatureReady(player2, new WildJhovall());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 3);
        harness.assertLife(player2, defenderLifeBefore);
        harness.assertInGraveyard(player1, "Pious Warrior");
        harness.assertOnBattlefield(player2, "Wild Jhovall");
    }

    @Test
    @DisplayName("Dealing combat damage to a player does not gain life")
    void unblockedAttackDoesNotGainLife() {
        addCreatureReady(player1, new PiousWarrior());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore);
        harness.assertLife(player2, defenderLifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Pious Warrior")
    void ignoresNoncombatDamage() {
        harness.addToBattlefield(player2, new PiousWarrior());
        harness.setHand(player1, List.of(new Thunderclap()));
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Pious Warrior"));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Pious Warrior");
    }
}
