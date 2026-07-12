package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CounterboreTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and exiles every same-name copy from graveyard, hand, and library")
    void countersAndExilesAllCopies() {
        Card castCopy = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        harness.setHand(player1, List.of(castCopy, handCopy));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setGraveyard(player1, new java.util.ArrayList<>(List.of(new GrizzlyBears())));
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new Plains());

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        // Spell countered — not on the stack, not on the battlefield.
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // All four Grizzly Bears (cast + hand + graveyard + library) exiled.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(4);

        // None remain in any of the searched zones.
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Leaves differently-named cards untouched")
    void leavesDifferentlyNamedCardsAlone() {
        Card castCopy = new GrizzlyBears();
        harness.setHand(player1, List.of(castCopy));
        harness.addMana(player1, ManaColor.GREEN, 2);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Plains());

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        // Plains is not named Grizzly Bears — stays in the library, never exiled.
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Counterbore goes to its caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card castCopy = new GrizzlyBears();
        harness.setHand(player1, List.of(castCopy));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Counterbore"));
    }
}
