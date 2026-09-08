package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagingRegisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 1 damage to a target player")
    void attackingDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingRegisaur());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Attacking deals 1 damage to a target creature")
    void attackingDealsDamageToCreature() {
        addCreatureReady(player1, new RagingRegisaur());
        harness.addToBattlefield(player2, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }
}
