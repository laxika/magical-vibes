package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SingeMindOgreTest extends BaseCardTest {

    private void castSingeMindOgre(java.util.UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new SingeMindOgre())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB makes target player lose life equal to the revealed card's mana value")
    void etbLosesLifeEqualToManaValue() {
        Card revealed = new GrizzlyBears(); // sole hand card -> revealed deterministically
        harness.setHand(player2, new ArrayList<>(List.of(revealed)));
        int manaValue = revealed.getManaValue();
        int lifeBefore = gd.getLife(player2.getId());

        castSingeMindOgre(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isLessThan(lifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - manaValue);
        // Revealing does not remove the card from the target's hand.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB does nothing when the target player's hand is empty")
    void etbEmptyHandNoLifeLoss() {
        harness.setHand(player2, new ArrayList<>());
        int lifeBefore = gd.getLife(player2.getId());

        castSingeMindOgre(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
