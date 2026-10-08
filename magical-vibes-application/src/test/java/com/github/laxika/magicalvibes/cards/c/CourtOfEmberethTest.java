package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.List;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtOfEmbereth.class, GrizzlyBears.class})
class CourtOfEmberethTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as the monarch and creates a Knight that contributes to its damage")
    void createsKnightAndDealsDamageAsMonarch() {
        harness.setHand(player1, List.of(new CourtOfEmbereth()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player1, "Knight")).isOne();
    }

    @Test
    @DisplayName("Creates a Knight but does not deal damage when its controller is not the monarch")
    void createsKnightWithoutDamageWhenNotMonarch() {
        harness.addToBattlefield(player1, new CourtOfEmbereth());
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Knight")).isOne();
    }
}
