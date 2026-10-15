package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CemeteryTampering.class, GrizzlyBears.class, AirElemental.class, Forest.class})
class CemeteryTamperingTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway 5 exiles one card face down and bottoms the rest")
    void hideawayExilesOneCard() {
        Card chosen = new GrizzlyBears();
        List<Card> library = new ArrayList<>(List.of(
                chosen, new AirElemental(), new AirElemental(), new AirElemental(), new AirElemental()));
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CemeteryTampering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent cemeteryTampering = findPermanent(player1, "Cemetery Tampering");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(cemeteryTampering.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(library.get(1), library.get(2), library.get(3), library.get(4));
    }

    @Test
    @DisplayName("May mill three cards and then may play the imprinted card at twenty cards in the graveyard")
    void millsAndOffersFreePlayAtTwentyCards() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(17));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental(), new AirElemental()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
    }

    @Test
    @DisplayName("Declining the mill leaves the graveyard and imprinted card unchanged")
    void decliningMillDoesNothingElse() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(17));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental(), new AirElemental()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(17);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the free-play offer leaves the imprinted card exiled")
    void decliningFreePlayLeavesCardExiled() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(17));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental(), new AirElemental()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Milling to fewer than twenty cards does not offer the imprinted card")
    void doesNotOfferFreePlayBelowTwentyCards() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(16));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental(), new AirElemental()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(19);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to mill still permits free play with twenty cards in the graveyard")
    void offersFreePlayAfterDecliningMillAtTwentyCards() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(20));
        Card topCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
    }

    @Test
    @DisplayName("An exiled land cannot be played after the available land play is used")
    void cannotExceedLandPlayLimit() {
        Card imprinted = new Forest();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(17));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental(), new AirElemental()));

        advanceToUpkeep(player1);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Milling a library with fewer than three cards still checks the graveyard threshold")
    void shortLibraryStillPermitsFreePlay() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        harness.setGraveyard(player1, cards(19));
        harness.setLibrary(player1, List.of(new AirElemental()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The opponent upkeep does not trigger milling")
    void doesNotTriggerDuringOpponentUpkeep() {
        Card imprinted = new GrizzlyBears();
        addCemeteryTamperingWithImprint(imprinted);
        Card topCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, cards(20));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
    }

    @Test
    @DisplayName("Hideaway with a one-card library automatically exiles that card face down")
    void hideawayWithOneCard() {
        Card chosen = new Forest();
        harness.setLibrary(player1, List.of(chosen));
        harness.setHand(player1, List.of(new CemeteryTampering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Cemetery Tampering");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getImprintedCard(source.getCard())).isSameAs(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Milling continues after the hideaway card is gone")
    void millsWithoutAnExiledCard() {
        harness.addToBattlefield(player1, new CemeteryTampering());
        harness.setGraveyard(player1, cards(17));
        harness.setLibrary(player1, List.of(new AirElemental(), new AirElemental(), new AirElemental()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addCemeteryTamperingWithImprint(Card imprinted) {
        Permanent cemeteryTampering = harness.addToBattlefieldAndReturn(player1, new CemeteryTampering());
        gd.setImprintedCard(cemeteryTampering.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return cemeteryTampering;
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

}
