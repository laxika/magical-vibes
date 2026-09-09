package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogsNullifier.class, GrizzlyBears.class, PathToExile.class})
class UlamogsNullifierTest extends BaseCardTest {

    @Test
    void movesTwoOpponentOwnedExiledCardsAndCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        PathToExile first = new PathToExile();
        PathToExile second = new PathToExile();
        harness.setExile(player2, List.of(first, second));
        castNullifierInResponseTo(bears);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void decliningLeavesSpellAndExiledCardsUnchanged() {
        GrizzlyBears bears = new GrizzlyBears();
        PathToExile first = new PathToExile();
        PathToExile second = new PathToExile();
        harness.setExile(player2, List.of(first, second));
        castNullifierInResponseTo(bears);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    void doesNotPromptWithoutTwoOpponentOwnedExiledCards() {
        GrizzlyBears bears = new GrizzlyBears();
        PathToExile onlyCard = new PathToExile();
        harness.setExile(player2, List.of(onlyCard));
        castNullifierInResponseTo(bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castNullifierInResponseTo(GrizzlyBears bears) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new UlamogsNullifier()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
    }
}
