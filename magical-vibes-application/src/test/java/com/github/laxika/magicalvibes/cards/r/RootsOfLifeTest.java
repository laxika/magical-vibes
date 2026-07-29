package com.github.laxika.magicalvibes.cards.r;

import java.util.List;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("An opponent's land of the chosen type becoming tapped gains 1 life")
    void opponentChosenTypeLandTapGainsLife() {
        Permanent roots = harness.addToBattlefieldAndReturn(player1, new RootsOfLife());
        roots.setChosenSubtype(CardSubtype.SWAMP);
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(swamp);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

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

    private void castRootsOfLife() {
        harness.setHand(player1, List.of(new RootsOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
