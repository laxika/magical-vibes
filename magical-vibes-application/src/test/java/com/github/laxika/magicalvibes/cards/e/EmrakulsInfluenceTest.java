package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.ItOfTheHorridSwarm;
import com.github.laxika.magicalvibes.cards.w.WoodlandPatrol;
import com.github.laxika.magicalvibes.cards.w.WretchedGryff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmrakulsInfluence.class, WretchedGryff.class, WoodlandPatrol.class,
        EternalScourge.class, ItOfTheHorridSwarm.class, EndlessOne.class})
class EmrakulsInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when a controller casts an Eldrazi creature with mana value 7")
    void drawsTwoCardsForLargeEldraziCreatureSpell() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new WoodlandPatrol(), new WoodlandPatrol(), new WoodlandPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Woodland Patrol");
    }

    @Test
    @DisplayName("Does not trigger for an Eldrazi creature with mana value less than 7")
    void doesNotTriggerForSmallEldraziCreatureSpell() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        WoodlandPatrol libraryCard = new WoodlandPatrol();
        harness.setHand(player1, List.of(new EternalScourge()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void drawsTwoCardsBeforeLargeEldraziResolves() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.setLibrary(player1, List.of(new WoodlandPatrol(), new WoodlandPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        while (gd.stack.size() > 1) {
            harness.passBothPriorities();
        }
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "It of the Horrid Swarm");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "It of the Horrid Swarm");
    }

    @Test
    void emergeStillUsesPrintedManaValue() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.addToBattlefield(player1, new WoodlandPatrol());
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new WoodlandPatrol(), new WoodlandPatrol(), new WoodlandPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithAlternateCost(player1, 0,
                List.of(harness.getPermanentId(player1, "Woodland Patrol")));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Wretched Gryff");
        harness.assertInGraveyard(player1, "Woodland Patrol");
    }

    @Test
    void opponentCastingLargeEldraziDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        WoodlandPatrol libraryCard = new WoodlandPatrol();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLibrary(player2, List.of(new WoodlandPatrol()));
        harness.setHand(player2, List.of(new WretchedGryff()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        WoodlandPatrol libraryCard = new WoodlandPatrol();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.enterBattlefieldAndReturn(player1, new WretchedGryff());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void nonEldraziCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.setHand(player1, List.of(new WoodlandPatrol()));
        WoodlandPatrol libraryCard = new WoodlandPatrol();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void chosenXBelowSevenDoesNotTrigger() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.setHand(player1, List.of(new EndlessOne()));
        WoodlandPatrol libraryCard = new WoodlandPatrol();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 6, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void chosenXCountsTowardEldraziSpellManaValue() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.setHand(player1, List.of(new EndlessOne()));
        harness.setLibrary(player1, List.of(new WoodlandPatrol(), new WoodlandPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        gs.playCard(gd, player1, 0, 7, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Endless One");
    }
}
