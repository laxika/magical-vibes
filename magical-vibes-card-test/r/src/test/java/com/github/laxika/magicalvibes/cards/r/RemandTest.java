package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Remand.class, Char.class, Combust.class, Watchwolf.class})
class RemandTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell into its owner's hand and draws a card")
    void countersSpellIntoOwnersHandAndDraws() {
        harness.setLibrary(player2, List.of(new Watchwolf()));

        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Remand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, watchwolf.getId());

        harness.assertInHand(player1, "Watchwolf");
        harness.assertInGraveyard(player2, "Remand");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Watchwolf");
    }

    @Test
    @DisplayName("Counters a noncreature spell into its owner's hand and draws a card")
    void countersNoncreatureSpellIntoOwnersHandAndDraws() {
        harness.setLibrary(player2, List.of(new Watchwolf()));
        harness.addToBattlefield(player2, new Watchwolf());

        Char charSpell = new Char();
        harness.setHand(player1, List.of(charSpell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Remand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Watchwolf"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, charSpell.getId());

        harness.assertInHand(player1, "Char");
        harness.assertInGraveyard(player2, "Remand");
        assertThat(harness.getGameData().playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Watchwolf");
    }

    @Test
    @DisplayName("Draws a card but does not return an uncounterable spell")
    void drawsWhenTargetSpellCannotBeCountered() {
        harness.setLibrary(player2, List.of(new Watchwolf()));
        harness.addToBattlefield(player2, new Watchwolf());

        Combust combust = new Combust();
        harness.setHand(player1, List.of(combust));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Remand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Watchwolf"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, combust.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Combust");
        harness.assertInGraveyard(player2, "Remand");
        assertThat(harness.getGameData().playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Watchwolf");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var watchwolf = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player2, List.of(new Remand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, watchwolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter your own spell and return it while drawing a card")
    void canCounterOwnSpell() {
        Watchwolf watchwolf = new Watchwolf();
        Watchwolf drawnCard = new Watchwolf();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(watchwolf, new Remand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, watchwolf.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(watchwolf, drawnCard);
        harness.assertNotOnBattlefield(player1, "Watchwolf");
        harness.assertNotInGraveyard(player1, "Watchwolf");
        harness.assertInGraveyard(player1, "Remand");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when its target leaves the stack before resolution")
    void doesNotDrawWhenTargetLeavesStack() {
        Watchwolf watchwolf = new Watchwolf();
        Watchwolf drawnCard = new Watchwolf();
        Remand firstRemand = new Remand();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLibrary(player2, List.of(new Watchwolf()));
        harness.setHand(player1, List.of(watchwolf, new Remand()));
        harness.setHand(player2, List.of(firstRemand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, watchwolf.getId());
        harness.castAndResolveInstant(player1, 0, watchwolf.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(watchwolf, drawnCard);
        assertThat(harness.getGameData().playerHands.get(player2.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Remand");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
