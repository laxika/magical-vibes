package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootsOfLife.class, Island.class, Swamp.class})
class RootsOfLifeTest extends BaseCardTest {

    // "As this enchantment enters, choose Island or Swamp.
    //  Whenever a land of the chosen type an opponent controls becomes tapped, you gain 1 life."

    @Test
    @DisplayName("Resolving offers only Island and Swamp as the land type choice")
    void resolvingOffersOnlyIslandAndSwamp() {
        castRootsOfLife();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("ISLAND", "SWAMP");
    }

    @Test
    @DisplayName("Choosing a type stores it on the permanent")
    void choosingTypeStoresIt() {
        castRootsOfLife();

        harness.handleListChoice(player1, "SWAMP");

        assertThat(findPermanent(player1, "Roots of Life").getChosenSubtype()).isEqualTo(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Choosing Island stores it on the permanent")
    void choosingIslandStoresIt() {
        castRootsOfLife();

        harness.handleListChoice(player1, "ISLAND");

        assertThat(findPermanent(player1, "Roots of Life").getChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("An opponent's land of the chosen type becoming tapped gains 1 life")
    void opponentChosenTypeLandTapGainsLife() {
        Permanent roots = harness.addToBattlefieldAndReturn(player1, new RootsOfLife());
        roots.setChosenSubtype(CardSubtype.SWAMP);
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(swamp);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An opponent's land of another type becoming tapped does not trigger")
    void opponentOtherTypeLandTapDoesNotTrigger() {
        Permanent roots = harness.addToBattlefieldAndReturn(player1, new RootsOfLife());
        roots.setChosenSubtype(CardSubtype.SWAMP);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(island);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping your own land of the chosen type does not trigger")
    void ownLandTapDoesNotTrigger() {
        Permanent roots = harness.addToBattlefieldAndReturn(player1, new RootsOfLife());
        roots.setChosenSubtype(CardSubtype.SWAMP);
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(swamp);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent tapping an Island for mana triggers after Island is chosen")
    void islandManaTapGainsLife() {
        castRootsOfLife();
        harness.handleListChoice(player1, "ISLAND");
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The same Swamp can trigger again after being untapped")
    void repeatedManaTapsGainLifeEachTime() {
        castRootsOfLife();
        harness.handleListChoice(player1, "SWAMP");
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        swamp.untap();
        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Two copies choosing the same type each trigger on a matching land")
    void multipleCopiesEachGainLife() {
        castRootsOfLife();
        harness.handleListChoice(player1, "SWAMP");
        castRootsOfLife();
        harness.handleListChoice(player1, "SWAMP");
        harness.addToBattlefield(player2, new Swamp());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A pending life gain trigger resolves even after Roots of Life leaves")
    void pendingTriggerSurvivesSourceRemoval() {
        castRootsOfLife();
        harness.handleListChoice(player1, "SWAMP");
        Permanent roots = findPermanent(player1, "Roots of Life");
        harness.addToBattlefield(player2, new Swamp());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, roots));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Roots of Life");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Each copy remembers its own chosen land type")
    void multipleCopiesKeepIndependentChoices() {
        castRootsOfLife();
        harness.handleListChoice(player1, "ISLAND");
        castRootsOfLife();
        harness.handleListChoice(player1, "SWAMP");
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.setLife(player1, 20);

        harness.tapPermanent(player2, 0);
        harness.passPriority(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);

        harness.tapPermanent(player2, 1);
        harness.passPriority(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    private void castRootsOfLife() {
        harness.castFromHand(player1, new RootsOfLife(), "{1}{G}{G}");
        harness.passBothPriorities();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
