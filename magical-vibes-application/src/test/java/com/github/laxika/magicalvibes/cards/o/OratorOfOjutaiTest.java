package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RapaciousDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OratorOfOjutai.class, RapaciousDragon.class, Island.class})
class OratorOfOjutaiTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Dragon is revealed from hand")
    void drawsWhenDragonIsRevealed() {
        OratorOfOjutai orator = new OratorOfOjutai();
        RapaciousDragon dragon = new RapaciousDragon();
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(orator, dragon));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon, drawnCard);
    }

    @Test
    @DisplayName("Draws a card when a Dragon was controlled as it was cast")
    void drawsWhenDragonWasControlledAsCast() {
        Permanent dragon = addCreatureReady(player1, new RapaciousDragon());
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Does not draw without a revealed or controlled Dragon")
    void doesNotDrawWithoutDragon() {
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
    }
}
