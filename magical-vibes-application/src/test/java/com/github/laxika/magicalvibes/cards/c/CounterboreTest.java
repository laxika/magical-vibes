package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Counterbore.class, BriarberryCohort.class, Plains.class})
class CounterboreTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell and exiles every same-name copy from graveyard, hand, and library")
    void countersAndExilesAllCopies() {
        Card castCopy = new BriarberryCohort();
        Card handCopy = new BriarberryCohort();
        harness.setHand(player1, List.of(castCopy, handCopy));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setGraveyard(player1, List.of(new BriarberryCohort()));
        harness.setLibrary(player1, List.of(new BriarberryCohort(), new Plains()));

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, castCopy.getId());

        // Spell countered — not on the stack, not on the battlefield.
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Briarberry Cohort"));
        harness.assertNotOnBattlefield(player1, "Briarberry Cohort");

        // All four Briarberry Cohorts (cast + hand + graveyard + library) exiled.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Briarberry Cohort"))
                .hasSize(4);

        // None remain in any of the searched zones.
        harness.assertNotInHand(player1, "Briarberry Cohort");
        harness.assertNotInGraveyard(player1, "Briarberry Cohort");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Briarberry Cohort"));
    }

    @Test
    @DisplayName("Leaves differently-named cards untouched")
    void leavesDifferentlyNamedCardsAlone() {
        Card castCopy = new BriarberryCohort();
        harness.setHand(player1, List.of(castCopy));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setLibrary(player1, List.of(new Plains()));

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, castCopy.getId());

        // Plains is not named Briarberry Cohort — stays in the library, never exiled.
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Fizzles without searching when the target spell leaves the stack")
    void fizzlesIfTargetLeavesStack() {
        Card castCopy = new BriarberryCohort();
        harness.setHand(player1, List.of(castCopy, new BriarberryCohort()));
        harness.setGraveyard(player1, List.of(new BriarberryCohort()));
        harness.setLibrary(player1, List.of(new BriarberryCohort()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());

        gd.stack.removeIf(stackEntry -> castCopy.getId().equals(stackEntry.getTargetableId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Briarberry Cohort"));
        harness.assertInHand(player1, "Briarberry Cohort");
        harness.assertInGraveyard(player1, "Briarberry Cohort");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Briarberry Cohort"));
        harness.assertInGraveyard(player2, "Counterbore");
    }

    @Test
    @DisplayName("Counterbore goes to its caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card castCopy = new BriarberryCohort();
        harness.setHand(player1, List.of(castCopy));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Counterbore()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, castCopy.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Counterbore");
    }
}
