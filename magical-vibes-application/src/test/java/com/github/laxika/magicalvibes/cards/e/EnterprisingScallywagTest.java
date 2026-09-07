package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnterprisingScallywag.class, ZuranOrb.class, Forest.class})
class EnterprisingScallywagTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token at your end step after descending")
    void createsTreasureAfterDescending() {
        harness.addToBattlefield(player1, new EnterprisingScallywag());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure token at your end step without descending")
    void doesNotCreateTreasureWithoutDescending() {
        harness.addToBattlefield(player1, new EnterprisingScallywag());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
