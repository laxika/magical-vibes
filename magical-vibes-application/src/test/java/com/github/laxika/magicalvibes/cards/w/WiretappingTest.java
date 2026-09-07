package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wiretapping.class, GrizzlyBears.class, AirElemental.class})
class WiretappingTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway 5 exiles one card face down and bottoms the rest")
    void hideawayExilesOneCard() {
        Card chosen = new GrizzlyBears();
        List<Card> library = new ArrayList<>(List.of(
                chosen,
                new AirElemental(), new AirElemental(), new AirElemental(), new AirElemental()));
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new Wiretapping()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent wiretapping = findPermanent(player1, "Wiretapping");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(wiretapping.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2), library.get(3), library.get(4));
    }

    @Test
    @DisplayName("Draws an extra card and offers the imprinted card at nine cards in hand")
    void drawsAndOffersFreePlayAtNineCards() {
        Card imprinted = new GrizzlyBears();
        addWiretappingWithImprint(imprinted);
        harness.setHand(player1, cards(8));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental()));

        advanceToDraw(player1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
    }

    @Test
    @DisplayName("Draws an extra card but does not offer the imprinted card below nine cards")
    void doesNotOfferFreePlayBelowNineCards() {
        Card imprinted = new GrizzlyBears();
        addWiretappingWithImprint(imprinted);
        harness.setHand(player1, cards(6));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the free-play offer leaves the imprinted card exiled")
    void decliningFreePlayLeavesCardExiled() {
        Card imprinted = new GrizzlyBears();
        addWiretappingWithImprint(imprinted);
        harness.setHand(player1, cards(8));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental()));

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers only during the controller's draw step")
    void doesNotTriggerOnOpponentsDrawStep() {
        Card imprinted = new GrizzlyBears();
        addWiretappingWithImprint(imprinted);
        harness.setHand(player1, cards(8));
        harness.setLibrary(player2, List.of(new AirElemental()));

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
    }

    private Permanent addWiretappingWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new Wiretapping());
        Permanent wiretapping = findPermanent(player1, "Wiretapping");
        gd.setImprintedCard(wiretapping.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return wiretapping;
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
