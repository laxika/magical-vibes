package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaneswalkersMirthTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the mana value of the randomly revealed card")
    void gainsLifeEqualToRevealedManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        Card revealed = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(revealed)));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Does nothing when the target opponent's hand is empty")
    void emptyHandNoLifeGain() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revealing a land gains no life")
    void landHasZeroManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMirth());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
