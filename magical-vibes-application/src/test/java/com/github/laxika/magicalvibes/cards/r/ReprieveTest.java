package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reprieve.class, JhovallRider.class, Island.class})
class ReprieveTest extends BaseCardTest {

    @Test
    void returnsTargetSpellToItsOwnersHandAndDrawsACard() {
        JhovallRider rider = new JhovallRider();
        harness.castFromHand(player1, rider, "{4}{W}");

        Island drawnCard = new Island();
        harness.setLibrary(player2, List.of(drawnCard));
        Reprieve reprieve = new Reprieve();
        harness.setHand(player2, List.of(reprieve));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rider.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jhovall Rider");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(reprieve.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Reprieve reprieve = new Reprieve();
        harness.setHand(player2, List.of(reprieve));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(reprieve);
        assertThat(gd.stack).isEmpty();
    }
}
