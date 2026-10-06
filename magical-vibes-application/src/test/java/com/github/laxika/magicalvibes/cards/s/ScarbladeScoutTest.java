package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScarbladeScout.class, Forest.class})
class ScarbladeScoutTest extends BaseCardTest {

    @Test
    void etbMillsTwoCardsFromControllerLibrary() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();

        harness.setHand(player1, List.of(new ScarbladeScout()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLibrary(player1, List.of(f1, f2));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardBefore + 2);
    }

    @Test
    void etbMillsOnlyAvailableCards() {
        Forest forest = new Forest();

        harness.setHand(player1, List.of(new ScarbladeScout()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLibrary(player1, List.of(forest));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void etbMillsOnlyTopTwoCardsAndLeavesOpponentLibraryAlone() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new ScarbladeScout()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void etbWithEmptyLibraryStillResolvesAndDoesNotLoseTheGame() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ScarbladeScout()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Scarblade Scout");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void unblockedCombatDamageGainsLifeForController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScarbladeScout());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }
}
