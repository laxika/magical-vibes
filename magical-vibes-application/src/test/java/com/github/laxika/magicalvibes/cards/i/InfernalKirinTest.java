package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MausoleumWanderer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalKirinTest extends BaseCardTest {

    @Test
    @DisplayName("An Arcane spell makes the target player discard all cards with its mana value")
    void arcaneSpellDiscardsMatchingManaValueCards() {
        addInfernalKirin();
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new SuntailHawk(), new SerraAngel())));

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Suntail Hawk", "Serra Angel");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("A Spirit spell makes the target player discard cards with mana value one")
    void spiritSpellDiscardsManaValueOneCards() {
        addInfernalKirin();
        harness.setHand(player1, List.of(new MausoleumWanderer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, new ArrayList<>(List.of(new SuntailHawk(), new GrizzlyBears())));

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addInfernalKirin();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void addInfernalKirin() {
        harness.addToBattlefield(player1, new InfernalKirin());
    }
}
