package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RMSTitanic.class, HillGiant.class})
class RMSTitanicTest extends BaseCardTest {

    @Test
    void crewAnimatesTitanicAndTapsCrew() {
        Permanent titanic = addReadyTitanic(player1);
        Permanent crew = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, titanic)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void combatDamageSacrificesTitanicAndCreatesTreasureEqualToDamage() {
        Permanent titanic = addReadyTitanic(player1);
        titanic.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "RMS Titanic");
        harness.assertInGraveyard(player1, "RMS Titanic");
        assertThat(findPermanents(player1, "Treasure")).hasSize(7);
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
}
