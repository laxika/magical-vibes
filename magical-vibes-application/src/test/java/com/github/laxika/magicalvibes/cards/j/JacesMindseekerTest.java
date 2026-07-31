package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JacesMindseekerTest extends BaseCardTest {

    private void castMindseekerTargetingOpponent() {
        harness.setHand(player1, new ArrayList<>(List.of(new JacesMindseeker())));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private List<Card> topFive(Card... top) {
        List<Card> library = new ArrayList<>(List.of(top));
        while (library.size() < 5) {
            library.add(new GrizzlyBears());
        }
        return library;
    }

    @Test
    @DisplayName("ETB mills five cards from target opponent")
    void etbMillsFive() {
        harness.setLibrary(player2, topFive());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castMindseekerTargetingOpponent();

        assertThat(deckBefore - gd.playerDecks.get(player2.getId()).size()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Accepting the may-cast casts a milled sorcery without paying its mana cost")
    void castsMilledSorceryForFree() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setLibrary(player2, topFive(counsel));

        castMindseekerTargetingOpponent();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve Counsel of the Soratami

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("A milled targeted instant is cast for free and prompts for its target")
    void castsMilledTargetedInstant() {
        Shock shock = new Shock();
        harness.setLibrary(player2, topFive(shock));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castMindseekerTargetingOpponent();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve Shock

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining leaves the milled spell in the opponent's graveyard")
    void decliningLeavesCardInGraveyard() {
        harness.setLibrary(player2, topFive(new CounselOfTheSoratami()));

        castMindseekerTargetingOpponent();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Only one milled spell may be cast — accepting one clears the other offers")
    void onlyOneSpellIsCast() {
        harness.setLibrary(player2, topFive(new CounselOfTheSoratami(), new CounselOfTheSoratami()));

        castMindseekerTargetingOpponent();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the cast spell

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("No offer when none of the milled cards is an instant or sorcery")
    void noOfferWithoutInstantOrSorcery() {
        harness.setLibrary(player2, topFive());

        castMindseekerTargetingOpponent();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
