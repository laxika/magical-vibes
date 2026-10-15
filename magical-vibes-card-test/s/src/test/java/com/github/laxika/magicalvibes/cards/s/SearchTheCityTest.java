package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchTheCity.class, Forest.class, Island.class, Plains.class, DrudgeBeetle.class, SunderingGrowth.class})
class SearchTheCityTest extends BaseCardTest {

    /** Casts and resolves Search the City, leaving {@code library}'s top five cards exiled with it. */
    private UUID resolveEtb(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SearchTheCity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return harness.getPermanentId(player1, "Search the City");
    }

    @Test
    @DisplayName("ETB exiles the top five cards face up, tracked with the enchantment")
    void etbExilesTopFiveFaceUp() {
        UUID permId = resolveEtb(List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(e -> !e.faceDown());
    }

    @Test
    @DisplayName("Playing a land with a matching name returns one exiled copy to hand")
    void playingMatchingLandReturnsExiledCopy() {
        UUID permId = resolveEtb(List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities(); // resolve the trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(4);
        harness.assertInHand(player1, "Forest");
        // Cards remain exiled, so the enchantment stays on the battlefield and no extra turn is queued.
        harness.assertOnBattlefield(player1, "Search the City");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Declining the may leaves the exiled cards alone")
    void decliningLeavesExiledCards() {
        UUID permId = resolveEtb(List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
    }

    @Test
    @DisplayName("Playing a card whose name matches nothing exiled does not trigger")
    void nonMatchingPlayDoesNotTrigger() {
        UUID permId = resolveEtb(List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
    }

    @Test
    @DisplayName("Casting a spell with a matching name triggers the same ability")
    void castingMatchingSpellTriggers() {
        UUID permId = resolveEtb(List.of(new DrudgeBeetle(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        harness.setHand(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the trigger (it is above the creature spell)
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(4);
        harness.assertInHand(player1, "Drudge Beetle");
    }

    @Test
    @DisplayName("Emptying the exile sacrifices the enchantment and grants an extra turn")
    void emptyingExileSacrificesAndGrantsExtraTurn() {
        UUID permId = resolveEtb(List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        for (int i = 0; i < 5; i++) {
            harness.setHand(player1, List.of(new Forest()));
            gd.landsPlayedThisTurn.remove(player1.getId());
            harness.playLand(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
        harness.assertNotOnBattlefield(player1, "Search the City");
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("A short library exiles only the available cards")
    void shortLibraryExilesAvailableCards() {
        UUID permId = resolveEtb(List.of(new Forest(), new Island()));

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Search the City");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Exiling no cards does not itself sacrifice the enchantment")
    void emptyLibraryDoesNotGrantExtraTurn() {
        UUID permId = resolveEtb(List.of());

        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
        harness.assertOnBattlefield(player1, "Search the City");
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("An opponent playing a matching card does not trigger the ability")
    void opponentPlayingMatchingCardDoesNotTrigger() {
        UUID permId = resolveEtb(List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
    }

    @Test
    @DisplayName("Destroying the source in response still returns the card but grants no extra turn")
    void destroyedSourceCannotGrantExtraTurn() {
        UUID permId = resolveEtb(List.of(new Forest()));
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.setHand(player2, List.of(new SunderingGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, permId);
        harness.assertInGraveyard(player1, "Search the City");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("After control changes, a matching card returns to its owner")
    void newControllerReturnsCardToOriginalOwner() {
        UUID permId = resolveEtb(List.of(new Forest()));
        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(permId)).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player2, "Forest");
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
        harness.assertInGraveyard(player1, "Search the City");
        assertThat(gd.extraTurns).containsExactly(player2.getId());
    }
}
