package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WojekInvestigator.class, GrizzlyBears.class})
class WojekInvestigatorTest extends BaseCardTest {

    @Test
    void investigatesForEachOpponentWithMoreCardsInHand() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void comparesHandsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.setHand(player2, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void equalHandSizesDoNotInvestigate() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of(new WojekInvestigator()));
        harness.setHand(player2, List.of(new WojekInvestigator()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatesOnlyOnceEvenWhenOpponentHasSeveralMoreCards() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new WojekInvestigator(), new WojekInvestigator(),
                new WojekInvestigator()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new WojekInvestigator()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void triggersWithEqualHandsAndInvestigatesIfControllerHasFewerOnResolution() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of(new WojekInvestigator()));
        harness.setHand(player2, List.of(new WojekInvestigator()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        harness.setHand(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void createdClueCanBeSacrificedForTwoManaToDrawACard() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new WojekInvestigator()));
        WojekInvestigator drawnCard = new WojekInvestigator();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        var clue = findPermanent(player1, "Clue");
        clue.tap();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
