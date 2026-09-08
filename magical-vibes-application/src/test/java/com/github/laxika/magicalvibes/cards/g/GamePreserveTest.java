package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GamePreserveTest extends BaseCardTest {

    @Test
    void putsEachRevealedCreatureOntoItsOwnersBattlefield() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card player1Creature = new GrizzlyBears();
        Card player2Creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Creature));
        harness.setLibrary(player2, List.of(player2Creature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void leavesAllRevealedCardsOnTopWhenOneIsNotACreature() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card player1Creature = new GrizzlyBears();
        Card player2Noncreature = new LightningBolt();
        harness.setLibrary(player1, List.of(player1Creature));
        harness.setLibrary(player2, List.of(player2Noncreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Noncreature);
    }

    @Test
    void doesNotTriggerOnAnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GamePreserve());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
    }
}
