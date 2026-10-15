package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.s.SecondLittlePig;
import com.github.laxika.magicalvibes.cards.t.ThirdLittlePig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroverOfTheSwine.class, FirstLittlePig.class, SecondLittlePig.class, ThirdLittlePig.class,
        ThayanEvokers.class})
class DroverOfTheSwineTest extends BaseCardTest {

    private static final String CONJURE_MODE =
            "Conjure a card of your choice from the Three Pigs spellbook onto the battlefield";
    private static final String RETURN_MODE =
            "Return up to three target Boar creature cards with different names from your graveyard to the battlefield";

    @Test
    void conjuresAChosenPigOntoTheBattlefield() {
        harness.castFromHand(player1, new DroverOfTheSwine(), "{2}{W}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CONJURE_MODE);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice.cards()).hasSize(3);
        Card chosenPig = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(chosenPig.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(chosenPig.getId());
    }

    @Test
    void returnsUpToThreeDistinctBoarsFromTheGraveyard() {
        Card firstPig = new FirstLittlePig();
        Card duplicateFirstPig = new FirstLittlePig();
        Card secondPig = new SecondLittlePig();
        Card thirdPig = new ThirdLittlePig();
        DroverOfTheSwine drover = new DroverOfTheSwine();
        harness.setGraveyard(player1, List.of(firstPig, duplicateFirstPig, secondPig, thirdPig));
        harness.castFromHand(player1, drover, "{2}{W}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                firstPig.getId(), duplicateFirstPig.getId(), secondPig.getId(), thirdPig.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstPig.getId(), duplicateFirstPig.getId(), secondPig.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(
                firstPig.getId(), secondPig.getId(), thirdPig.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(
                        drover.getId(), firstPig.getId(), secondPig.getId(), thirdPig.getId());
    }

    @Test
    void canChooseNoTargetsEvenWhenBoarsAreAvailable() {
        Card pig = new FirstLittlePig();
        harness.setGraveyard(player1, List.of(pig));
        harness.castFromHand(player1, new DroverOfTheSwine(), "{2}{W}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(pig);
        harness.assertNotOnBattlefield(player1, "First Little Pig");
    }

    @Test
    void onlyOffersBoarsFromItsControllersGraveyardAndCanReturnOne() {
        Card pig = new FirstLittlePig();
        Card nonBoar = new DroverOfTheSwine();
        Card opponentsPig = new SecondLittlePig();
        harness.setGraveyard(player1, List.of(pig, nonBoar));
        harness.setGraveyard(player2, List.of(opponentsPig));
        harness.castFromHand(player1, new DroverOfTheSwine(), "{2}{W}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(pig.getId());
        harness.handleMultipleCardsChosen(player1, List.of(pig.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "First Little Pig");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonBoar);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsPig);
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherTargetLeavesTheGraveyard() {
        Card firstPig = new FirstLittlePig();
        Card secondPig = new SecondLittlePig();
        harness.setGraveyard(player1, List.of(firstPig, secondPig));
        harness.castFromHand(player1, new DroverOfTheSwine(), "{2}{W}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(firstPig.getId(), secondPig.getId()));

        harness.setGraveyard(player1, List.of(secondPig));
        harness.setExile(player1, List.of(firstPig));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "First Little Pig");
        harness.assertOnBattlefield(player1, "Second Little Pig");
    }

    @Test
    void conjuringTheChosenPigTriggersConjureAbilities() {
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.castFromHand(player1, new DroverOfTheSwine(), "{2}{W}{B}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CONJURE_MODE);
        harness.passBothPriorities();
        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card pig = choice.cards().stream()
                .filter(card -> card.getName().equals("First Little Pig"))
                .findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(pig.getId()));
        harness.passBothPriorities();

        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
