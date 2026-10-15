package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlabasterDragon.class, PsychogenicProbe.class})
class AlabasterDragonTest extends BaseCardTest {

    @Test
    @DisplayName("The death ability causes a library shuffle even with only one card")
    void deathAbilityTriggersShuffleObserver() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new PsychogenicProbe());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new AlabasterDragon());
        dragon.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Alabaster Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon.getCard());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The owner's library is still shuffled if the dragon is exiled before its death ability resolves")
    void exiledDragonStillCausesOwnersLibraryToShuffle() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new PsychogenicProbe());
        AlabasterDragon dragonCard = new AlabasterDragon();
        dragonCard.setOwnerId(player1.getId());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, dragonCard);
        dragon.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Alabaster Dragon");
        assertThat(gd.stack).hasSize(1);

        // Model graveyard exile while the death ability is pending.
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(dragonCard));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Alabaster Dragon");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("When Alabaster Dragon dies, a triggered ability shuffles it from the graveyard into its owner's library")
    void diesThenTriggerShufflesIntoLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new AlabasterDragon());
        // Mark lethal damage on the 4/4 and let state-based actions destroy it.
        dragon.setMarkedDamage(4);

        harness.runStateBasedActions();

        // It first enters the graveyard, then its death trigger waits on the stack.
        harness.assertInGraveyard(player1, "Alabaster Dragon");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        // After the trigger resolves, the dragon is shuffled into its owner's library.
        harness.assertNotInGraveyard(player1, "Alabaster Dragon");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Alabaster Dragon"));
    }

    @Test
    @DisplayName("When controlled by another player, Alabaster Dragon shuffles into its owner's library")
    void diesThenTriggerShufflesIntoOwnersLibraryWhenControlledByOpponent() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        AlabasterDragon dragonCard = new AlabasterDragon();
        dragonCard.setOwnerId(player1.getId());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, dragonCard);
        dragon.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Alabaster Dragon");
        harness.assertNotInGraveyard(player2, "Alabaster Dragon");
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Alabaster Dragon");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragonCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(dragonCard.getId()));
    }
}
