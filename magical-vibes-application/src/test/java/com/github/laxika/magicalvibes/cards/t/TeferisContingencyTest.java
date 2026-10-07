package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisContingency.class, ThinkTwice.class})
class TeferisContingencyTest extends BaseCardTest {

    @Test
    void countersSpellAndTaxesMatchingCardsInAllThreeZones() {
        ThinkTwice castCopy = new ThinkTwice();
        ThinkTwice handCopy = new ThinkTwice();
        ThinkTwice libraryCopy = new ThinkTwice();
        ThinkTwice libraryFiller = new ThinkTwice();
        harness.setHand(player1, List.of(castCopy, handCopy));
        harness.setLibrary(player1, List.of(libraryCopy, libraryFiller));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        harness.setHand(player2, List.of(new TeferisContingency()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, castCopy.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
    }

    @Test
    void doesNotTaxMatchingCardsInTheCounterControllersHand() {
        ThinkTwice target = new ThinkTwice();
        ThinkTwice unaffected = new ThinkTwice();
        harness.setHand(player1, List.of(target));
        harness.setHand(player2, List.of(new TeferisContingency(), unaffected));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Think Twice");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);

        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getCard().getId()).isEqualTo(unaffected.getId()));
    }

    @Test
    void taxesCardsAlreadyInGraveyardWhenTargetIsExiledByFlashback() {
        ThinkTwice target = new ThinkTwice();
        ThinkTwice graveyardCopy = new ThinkTwice();
        harness.setGraveyard(player1, List.of(target, graveyardCopy));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new TeferisContingency()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCopy);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getCard().getId()).isEqualTo(graveyardCopy.getId()));
    }
}
