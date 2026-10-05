package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JudoonEnforcers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RMSTitanic.class, JudoonEnforcers.class})
class RMSTitanicTest extends BaseCardTest {

    @Test
    void crewAnimatesTitanicAndTapsCrew() {
        Permanent titanic = addReadyTitanic(player1);
        Permanent crew = addCreatureReady(player1, new JudoonEnforcers());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, titanic)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void combatDamageSacrificesTitanicAndCreatesTreasureEqualToDamage() {
        Permanent titanic = addCrewedTitanic();
        titanic.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "RMS Titanic");
        harness.assertInGraveyard(player1, "RMS Titanic");
        assertThat(findPermanents(player1, "Treasure")).hasSize(7);
        harness.assertLife(player2, 13);
    }

    @Test
    void treasureCountUsesDamageDealtRatherThanPrintedPower() {
        Permanent titanic = addCrewedTitanic();
        titanic.setPowerModifier(-3);
        titanic.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "RMS Titanic");
        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    void createsTreasureEvenWhenTitanicLeavesBeforeTriggerResolves() {
        Permanent titanic = addCrewedTitanic();
        titanic.setAttacking(true);

        harness.resolveCombatDamage();
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(titanic);
        gd.playerGraveyards.get(player1.getId()).add(titanic.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(7);
        harness.assertInGraveyard(player1, "RMS Titanic");
    }

    @Test
    void zeroCombatDamageDoesNotSacrificeTitanicOrCreateTreasure() {
        Permanent titanic = addCrewedTitanic();
        titanic.setPowerModifier(-7);
        titanic.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "RMS Titanic")).containsExactly(titanic);
    }

    @Test
    void doesNotTriggerWithoutCombatDamage() {
        addReadyTitanic(player1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "RMS Titanic")).hasSize(1);
    }

    private Permanent addReadyTitanic(Player player) {
        return addCreatureReady(player, new RMSTitanic());
    }

    private Permanent addCrewedTitanic() {
        Permanent titanic = addReadyTitanic(player1);
        addCreatureReady(player1, new JudoonEnforcers());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return titanic;
    }
}
