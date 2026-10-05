package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacesMindseeker.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class})
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

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the milled spell in the opponent's graveyard")
    void decliningLeavesCardInGraveyard() {
        harness.setLibrary(player2, topFive(new CounselOfTheSoratami()));

        castMindseekerTargetingOpponent();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Counsel of the Soratami");
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

    @Test
    void doesNotOfferSpellAlreadyInOpponentsGraveyard() {
        CounselOfTheSoratami oldSpell = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(oldSpell));
        harness.setLibrary(player2, topFive());

        castMindseekerTargetingOpponent();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6).contains(oldSpell);
    }

    @Test
    void emptyOpponentLibraryDoesNotOfferSpell() {
        harness.setLibrary(player2, List.of());

        castMindseekerTargetingOpponent();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void millsAllCardsWhenOpponentHasFewerThanFive() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setLibrary(player2, List.of(counsel, new GrizzlyBears()));

        castMindseekerTargetingOpponent();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(counsel);
    }

    @Test
    void decliningFirstSpellAllowsCastingSecondSpell() {
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        CounselOfTheSoratami second = new CounselOfTheSoratami();
        harness.setLibrary(player2, topFive(first, second));

        castMindseekerTargetingOpponent();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(second.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(RestInPeace.class)
    void canCastMilledSpellExiledByRestInPeace() {
        harness.addToBattlefield(player1, new RestInPeace());
        harness.setLibrary(player2, topFive(new CounselOfTheSoratami()));

        castMindseekerTargetingOpponent();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @CardUsed(AltarsReap.class)
    void mandatorySacrificeIsPaidBeforeMilledSpellResolves() {
        harness.setLibrary(player2, topFive(new AltarsReap()));

        castMindseekerTargetingOpponent();
        UUID mindseekerId = harness.getPermanentId(player1, "Jace's Mindseeker");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, mindseekerId);
        harness.assertNotOnBattlefield(player1, "Jace's Mindseeker");
        harness.assertInGraveyard(player1, "Jace's Mindseeker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
