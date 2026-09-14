package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.l.LandGrant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GamePreserve.class, DeadlyInsect.class, LandGrant.class})
class GamePreserveTest extends BaseCardTest {

    @Test
    void putsEachRevealedCreatureOntoItsOwnersBattlefield() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card player1Creature = new DeadlyInsect();
        Card player2Creature = new DeadlyInsect();
        harness.setLibrary(player1, List.of(player1Creature));
        harness.setLibrary(player2, List.of(player2Creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Deadly Insect")).hasSize(1);
        assertThat(findPermanents(player2, "Deadly Insect")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void leavesAllRevealedCardsOnTopWhenOneIsNotACreature() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card player1Creature = new DeadlyInsect();
        Card player2Noncreature = new LandGrant();
        harness.setLibrary(player1, List.of(player1Creature));
        harness.setLibrary(player2, List.of(player2Noncreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deadly Insect");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Noncreature);
    }

    @Test
    void putsAvailableCreatureOntoBattlefieldWhenAnotherLibraryIsEmpty() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card creature = new DeadlyInsect();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.<Card>of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Deadly Insect")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerOnAnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card creature = new DeadlyInsect();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(new LandGrant()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deadly Insect");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }
}
