package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HolographicDouble.class, GrizzlyBears.class, Shock.class})
class HolographicDoubleTest extends BaseCardTest {

    @Test
    void exilesItselfAndConjuresASelectedCreatureDuplicateIntoHand() {
        Card holographicDouble = new HolographicDouble();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setHand(player1, List.of(holographicDouble, bears, shock));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(holographicDouble);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        PendingInteraction.RevealedHandChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock", "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .doesNotHaveDuplicates();
    }

    @Test
    void doesNotOfferNoncreatureCards() {
        Card holographicDouble = new HolographicDouble();
        Card shock = new Shock();
        harness.setHand(player1, List.of(holographicDouble, shock));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(holographicDouble);
    }
}
