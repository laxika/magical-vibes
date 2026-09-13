package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicPage;
import com.github.laxika.magicalvibes.cards.g.GoblinLackey;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireAnts.class, GoblinLackey.class, AngelicPage.class, GorillaWarrior.class})
class FireAntsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each other creature without flying, sparing flyers")
    void damagesOnlyOtherNonFlyers() {
        Permanent ants = addCreatureReady(player1, new FireAnts());
        harness.addToBattlefield(player2, new GoblinLackey());
        harness.addToBattlefield(player2, new AngelicPage());
        harness.addToBattlefield(player2, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        assertThat(ants.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fire Ants");
        assertThat(ants.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Goblin Lackey");
        harness.assertOnBattlefield(player2, "Angelic Page");
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Also damages other non-flying creatures its controller controls")
    void damagesOtherNonFlyersYouControl() {
        addCreatureReady(player1, new FireAnts());
        Permanent gorilla = addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gorilla.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        addCreatureReady(player1, new FireAnts());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
