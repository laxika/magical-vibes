package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ScionOfUgin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OratorOfOjutai.class, ScionOfUgin.class, Island.class})
class OratorOfOjutaiTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Dragon is revealed from hand")
    void drawsWhenDragonIsRevealed() {
        OratorOfOjutai orator = new OratorOfOjutai();
        ScionOfUgin dragon = new ScionOfUgin();
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(orator, dragon));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon, drawnCard);
    }

    @Test
    @DisplayName("Draws a card when a Dragon was controlled as it was cast")
    void drawsWhenDragonWasControlledAsCast() {
        Permanent dragon = addCreatureReady(player1, new ScionOfUgin());
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void doesNotTriggerWithoutDragonAtCastingTime() {
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Orator of Ojutai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillDrawsWhenControlledDragonDiesBeforeResolution() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ScionOfUgin());
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        dragon.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Scion of Ugin");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenDragonArrivesAfterCasting() {
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.enterBattlefieldAndReturn(player1, new ScionOfUgin());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void drawsOnlyOnceWhenDragonIsBothRevealedAndControlled() {
        harness.addToBattlefield(player1, new ScionOfUgin());
        ScionOfUgin revealedDragon = new ScionOfUgin();
        Island drawnCard = new Island();
        Island remainingCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai(), revealedDragon));
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealedDragon, drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void doesNotTriggerWhenEnteringWithoutBeingCastEvenWithDragon() {
        harness.addToBattlefield(player1, new ScionOfUgin());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player1, new OratorOfOjutai());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void opponentsDragonDoesNotQualify() {
        harness.addToBattlefield(player2, new ScionOfUgin());
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new OratorOfOjutai()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }
}
