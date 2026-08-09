package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from the targeted spell and puts the activation cost on top of the library")
    void preventsTargetedSpellDamage() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        Card chosenCard = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(chosenCard));
        harness.setHand(player2, List.of(shock));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosenCard);
    }
}
