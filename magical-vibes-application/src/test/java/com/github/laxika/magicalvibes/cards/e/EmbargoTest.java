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
}
