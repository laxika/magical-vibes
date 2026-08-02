package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterdictTest extends BaseCardTest {

    private void addReadyInterdict() {
        harness.setHand(player1, List.of(new Interdict()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    /** Rod of Ruin: "{3}, {T}: Rod of Ruin deals 1 damage to any target." */
    private RodOfRuin addRod() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        return rod;
    }

    @Test
    @DisplayName("Counters the targeted activated ability and draws a card")
    void countersAbilityAndDraws() {
        addReadyInterdict();
        RodOfRuin rod = addRod();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, rod.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
        // Interdict left the hand and drew one card, so the hand size is unchanged minus the cast.
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The ability's source can't activate its abilities again this turn")
    void locksSourcePermanentForTheTurn() {
        addReadyInterdict();
        RodOfRuin rod = addRod();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        harness.castInstant(player1, 0, rod.getId());
        harness.passBothPriorities();

        // Untap the Rod so only the Interdict lock — not its tap cost — can stop the activation.
        findPermanent(player2, "Rod of Ruin").untap();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotTargetSpell() {
        addReadyInterdict();

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, shockId))
                .isInstanceOf(IllegalStateException.class);
    }
}
