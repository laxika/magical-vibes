package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Scrapwork Mutt")
class ScrapworkMuttTest extends BaseCardTest {

    @Test
    @DisplayName("When Scrapwork Mutt enters, accepting the may ability discards then draws")
    void acceptEtbMayDiscardsThenDraws() {
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new ScrapworkMutt(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Forest");
    }

    @Test
    @DisplayName("When Scrapwork Mutt enters, declining the may ability does nothing")
    void declineEtbMayDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new ScrapworkMutt(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Unearth returns Scrapwork Mutt with haste")
    void unearthReturnsWithHaste() {
        ScrapworkMutt mutt = new ScrapworkMutt();
        harness.setGraveyard(player1, List.of(mutt));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Scrapwork Mutt");
        assertThat(permanent.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Scrapwork Mutt");
    }

    @Test
    @DisplayName("Unearthed Scrapwork Mutt is exiled at the next end step")
    void unearthExilesAtNextEndStep() {
        ScrapworkMutt mutt = new ScrapworkMutt();
        harness.setGraveyard(player1, List.of(mutt));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Scrapwork Mutt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Scrapwork Mutt"));
    }

    @Test
    @DisplayName("Unearth can only be activated at sorcery speed")
    void unearthOnlyAtSorcerySpeed() {
        ScrapworkMutt mutt = new ScrapworkMutt();
        harness.setGraveyard(player1, List.of(mutt));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Scrapwork Mutt");
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
