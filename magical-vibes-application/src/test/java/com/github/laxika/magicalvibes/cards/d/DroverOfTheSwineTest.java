package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.cards.s.SecondLittlePig;
import com.github.laxika.magicalvibes.cards.t.ThirdLittlePig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroverOfTheSwine.class, FirstLittlePig.class, SecondLittlePig.class, ThirdLittlePig.class})
class DroverOfTheSwineTest extends BaseCardTest {

    private static final String CONJURE_MODE =
            "Conjure a card of your choice from the Three Pigs spellbook onto the battlefield";
    private static final String RETURN_MODE =
            "Return up to three target Boar creature cards with different names from your graveyard to the battlefield";

    @Test
    void conjuresAChosenPigOntoTheBattlefield() {
        harness.setHand(player1, List.of(new DroverOfTheSwine()));
        addDroverMana();

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(drover));
        addDroverMana();

        harness.castCreature(player1, 0);
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

    private void addDroverMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
