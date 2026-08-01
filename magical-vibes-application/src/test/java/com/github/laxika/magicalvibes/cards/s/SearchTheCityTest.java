package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTheCityTest extends BaseCardTest {

    /** Casts and resolves Search the City, leaving {@code library}'s top five cards exiled with it. */
    private UUID resolveEtb(List<Card> library) {
        harness.setLibrary(player1, new ArrayList<>(library));
        harness.setHand(player1, List.of(new SearchTheCity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
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
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        // Cards remain exiled, so the enchantment stays on the battlefield and no extra turn is queued.
        assertThat(harness.getPermanentId(player1, "Search the City")).isNotNull();
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
        UUID permId = resolveEtb(List.of(new GrizzlyBears(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Island()));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the trigger (it is above the creature spell)
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Search the City"));
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }
}
