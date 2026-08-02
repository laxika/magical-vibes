package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquelchTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability and draws a card")
    void countersActivatedAbilityAndDraws() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.setHand(player1, List.of(new Squelch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, rod.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
        // Squelch left the hand, then drew one card: net hand size is unchanged minus the cast card.
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new Squelch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, shockId))
                .isInstanceOf(IllegalStateException.class);
    }
}
