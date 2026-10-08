package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurrencyConverter.class, Forest.class, GrizzlyBears.class})
class CurrencyConverterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws, discards, and may exile the discarded card with Currency Converter")
    void discardsAndTracksCardWithConverter() {
        Permanent converter = addConverter();
        Card discarded = new GrizzlyBears();
        setUpDiscard(converter, discarded);

        assertThat(gd.findExiledCard(discarded.getId())).isNotNull();
        assertThat(gd.findExiledCard(discarded.getId()).sourcePermanentId()).isEqualTo(converter.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
    }

    @Test
    @DisplayName("Returns an exiled land to its owner's graveyard and creates a Treasure")
    void landCreatesTreasure() {
        Permanent converter = addConverter();
        Card forest = new Forest();
        gd.addToExile(player1.getId(), forest, converter.getId());

        activateConversion(converter, forest.getId());

        assertThat(gd.findExiledCard(forest.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
    }

    @Test
    @DisplayName("Returns an exiled nonland to its owner's graveyard and creates a Rogue")
    void nonlandCreatesRogue() {
        Permanent converter = addConverter();
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, converter.getId());

        activateConversion(converter, bears.getId());

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Conversion can be activated with no cards exiled and does nothing")
    void conversionCanBeActivatedWithoutExiledCards() {
        Permanent converter = addConverter();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 1, null, null);
        assertThat(converter.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
    }

    @Test
    @DisplayName("Conversion chooses among linked cards as the ability resolves")
    void choosesExiledCardDuringResolution() {
        Permanent converter = addConverter();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), forest, converter.getId());
        gd.addToExile(player1.getId(), bears, converter.getId());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 1, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(com.github.laxika.magicalvibes.model.PendingInteraction.ExiledCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Declining exile leaves the discarded card in the graveyard")
    void mayDeclineExile() {
        Permanent converter = addConverter();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.findExiledCard(discarded.getId())).isNull();
    }

    @Test
    @DisplayName("Conversion returns a card to its owner and gives the controller the token")
    void conversionRespectsCardOwner() {
        Permanent converter = addConverter();
        Card forest = new Forest();
        gd.addToExile(player2.getId(), forest, converter.getId());
        activateConversion(converter, forest.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private Permanent addConverter() {
        return harness.addToBattlefieldAndReturn(player1, new CurrencyConverter());
    }

    @Test
    @DisplayName("A discarded card that leaves and reenters the graveyard is a new object")
    void cannotExileCardAfterItLeavesAndReentersGraveyard() {
        Permanent converter = addConverter();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        gd.playerGraveyards.get(player1.getId()).remove(discarded);
        gd.addToExile(player1.getId(), discarded);
        gd.removeFromExile(discarded.getId());
        harness.setGraveyard(player1, List.of(discarded));
        gd.markGraveyardEntry(discarded);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.findExiledCard(discarded.getId())).isNull();
    }

    private void setUpDiscard(Permanent converter, Card discarded) {
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    private void activateConversion(Permanent converter, UUID cardId) {
        converter.untap();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.ExiledCardChoice) {
            harness.handleMultipleCardsChosen(player1, List.of(cardId));
        }
    }
}
