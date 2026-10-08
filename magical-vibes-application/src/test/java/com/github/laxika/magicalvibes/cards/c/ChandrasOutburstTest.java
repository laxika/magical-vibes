package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasOutburst.class, ChandraBoldPyromancer.class})
class ChandrasOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player")
    void deals4DamageToTargetPlayer() {
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Finds named card in graveyard and puts it into hand")
    void findsNamedCardInGraveyard() {
        ChandraBoldPyromancer chandraBold = new ChandraBoldPyromancer();

        harness.setGraveyard(player1, List.of(chandraBold));
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(chandraBold.getId()));

        // Damage dealt
        harness.assertLife(player2, 16);
        // Named card moved from graveyard to hand
        harness.assertInHand(player1, "Chandra, Bold Pyromancer");
        harness.assertNotInGraveyard(player1, "Chandra, Bold Pyromancer");
    }

    @Test
    @DisplayName("Does not find named card when not in graveyard or library")
    void doesNotFindNamedCard() {
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Damage still dealt
        harness.assertLife(player2, 16);
        // No card found
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void dealsFourDamageToPlaneswalker() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    void findsNamedCardInLibrary() {
        var chandra = new ChandraBoldPyromancer();
        harness.setLibrary(player1, List.of(chandra));
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chandra.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chandra);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    void mayFailToFindNamedCardInLibrary() {
        var chandra = new ChandraBoldPyromancer();
        harness.setLibrary(player1, List.of(chandra));
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chandra);
        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotForceGraveyardCopyWhenLibraryCopyIsAvailable() {
        var graveyardCopy = new ChandraBoldPyromancer();
        var libraryCopy = new ChandraBoldPyromancer();
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCopy);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    void doesNotSearchWhenOnlyTargetLeavesBattlefield() {
        var target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        var searchableCard = new ChandraBoldPyromancer();
        harness.setLibrary(player1, List.of(searchableCard));
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(searchableCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Chandra's Outburst");
    }
}
