package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Embargo.class, DeadlyInsect.class, Swamp.class})
class EmbargoTest extends BaseCardTest {

    @Test
    @DisplayName("Nonland permanents do not untap, but lands do")
    void nonlandPermanentsDoNotUntap() {
        harness.addToBattlefield(player1, new Embargo());
        Permanent creature = addCreatureReady(player2, new DeadlyInsect());
        Permanent land = addCreatureReady(player2, new Swamp());
        creature.tap();
        land.tap();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Embargo also prevents its controller's nonland permanents from untapping")
    void controllerNonlandPermanentsDoNotUntap() {
        harness.addToBattlefield(player1, new Embargo());
        Permanent creature = addCreatureReady(player1, new DeadlyInsect());
        creature.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller loses 2 life during their upkeep")
    void controllerLosesLifeDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new Embargo());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Embargo does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new Embargo());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tapped Embargo stays tapped and still locks nonland permanents and triggers")
    void tappedEmbargoStillFunctions() {
        Permanent embargo = harness.addToBattlefieldAndReturn(player1, new Embargo());
        Permanent creature = addCreatureReady(player1, new DeadlyInsect());
        embargo.tap();
        creature.tap();
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(embargo.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each Embargo independently causes its controller to lose 2 life")
    void multipleCopiesEachCauseLifeLoss() {
        harness.addToBattlefield(player1, new Embargo());
        harness.addToBattlefield(player1, new Embargo());
        harness.addToBattlefield(player2, new Embargo());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Nonland permanents untap normally once Embargo leaves the battlefield")
    void untapLockEndsWhenEmbargoLeaves() {
        Permanent embargo = harness.addToBattlefieldAndReturn(player1, new Embargo());
        Permanent creature = addCreatureReady(player2, new DeadlyInsect());
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(embargo);
        gd.playerGraveyards.get(player1.getId()).add(embargo.getCard());
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }
}
