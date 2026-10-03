package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Ixidron;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BazaarOfWonders.class, BayFalcon.class, Ixidron.class})
class BazaarOfWondersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield exiles all graveyards")
    void enteringExilesAllGraveyards() {
        harness.setGraveyard(player1, List.of(new BayFalcon()));
        harness.setGraveyard(player2, List.of(new BayFalcon()));

        harness.setHand(player1, List.of(new BazaarOfWonders()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell is countered when a card with the same name is in a graveyard")
    void countersSpellWithSameNameInGraveyard() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.setGraveyard(player2, List.of(new BayFalcon()));

        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Bay Falcon"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A spell is countered when a nontoken permanent with the same name is on the battlefield")
    void countersSpellWithSameNameOnBattlefield() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.addToBattlefield(player1, new BayFalcon());

        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        harness.assertInGraveyard(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("A spell resolves normally when no card with its name is elsewhere")
    void spellResolvesWhenNameIsUnique() {
        harness.addToBattlefield(player1, new BazaarOfWonders());

        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Bay Falcon");
        harness.assertNotInGraveyard(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("A token with the same name on the battlefield does not cause a counter")
    void tokenWithSameNameDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        BayFalcon token = new BayFalcon();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Bay Falcon");
        harness.assertNotInGraveyard(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("The name check uses the game state when the trigger resolves")
    void matchingGraveyardCardAddedAfterCastStillCountersSpell() {
        harness.addToBattlefield(player1, new BazaarOfWonders());

        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.setGraveyard(player1, List.of(new BayFalcon()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Bay Falcon");
        harness.assertInGraveyard(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("The controller's spells are also countered by matching graveyard cards")
    void countersControllersSpell() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.setGraveyard(player2, List.of(new BayFalcon()));
        harness.setHand(player1, List.of(new BayFalcon()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertInGraveyard(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("A matching graveyard card removed before resolution no longer counters the spell")
    void matchingGraveyardCardRemovedBeforeResolutionDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.setGraveyard(player1, List.of(new BayFalcon()));
        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Bay Falcon");
        harness.assertNotInGraveyard(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("An existing Bazaar counters a second Bazaar before it can enter")
    void countersSecondBazaar() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.setGraveyard(player1, List.of(new BayFalcon()));
        harness.setHand(player2, List.of(new BazaarOfWonders()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bazaar of Wonders");
        harness.assertNotOnBattlefield(player2, "Bazaar of Wonders");
        harness.assertInGraveyard(player2, "Bazaar of Wonders");
        harness.assertInGraveyard(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("Entering exiles cards to their owners' exile zones and they no longer cause counters")
    void exiledGraveyardMatchesDoNotCounterLaterSpells() {
        BayFalcon ownCard = new BayFalcon();
        BayFalcon opposingCard = new BayFalcon();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.setHand(player1, List.of(new BazaarOfWonders(), new BayFalcon()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(ownCard);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        }).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(opposingCard);
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
        });

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bay Falcon");
        harness.assertNotInGraveyard(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("A face-down permanent has no name and does not match its underlying card")
    void faceDownPermanentDoesNotCounterSpellWithUnderlyingCardName() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        var falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        harness.setHand(player1, List.of(new Ixidron()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(falcon.isFaceDown()).isTrue();

        harness.setHand(player2, List.of(new BayFalcon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Bay Falcon");
        harness.assertNotInGraveyard(player2, "Bay Falcon");
    }
}
