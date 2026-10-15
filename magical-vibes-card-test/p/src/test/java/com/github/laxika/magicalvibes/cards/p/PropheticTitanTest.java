package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.s.SanctumWeaver;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PropheticTitan.class, Forest.class, GrizzlyBears.class, Island.class,
        OrnithopterOfParadise.class, SanctumWeaver.class})
class PropheticTitanTest extends BaseCardTest {

    private static final String DAMAGE_MODE = "This creature deals 4 damage to any target.";
    private static final String LIBRARY_MODE =
            "Look at the top four cards of your library. Put one of them into your hand and the rest on the bottom of your library in a random order.";

    @Test
    void withoutDeliriumChoosesOnlyOneMode() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Island(), new GrizzlyBears()));
        castTitan();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(DAMAGE_MODE, LIBRARY_MODE);
        assertThat(choice.options()).doesNotContain("Done");
        assertThat(choice.prompt()).contains("Choose one.");

        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void withDeliriumChoosesBothModes() {
        setDelirium();
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Island(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTitan();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(DAMAGE_MODE, LIBRARY_MODE);

        harness.handleListChoice(player1, DAMAGE_MODE);
        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice.options()).containsExactly(LIBRARY_MODE);
        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void damageModeCanTargetAPlayerWithoutLookingAtLibrary() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        castTitan();

        harness.handleListChoice(player1, DAMAGE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void deliriumDoesNotAllowFinishingAfterOnlyOneMode() {
        setDelirium();
        harness.setLibrary(player1, List.of());
        castTitan();

        harness.handleListChoice(player1, LIBRARY_MODE);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Done"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, DAMAGE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 16);
    }

    @Test
    void losingDeliriumAfterChoosingBothDoesNotRemoveEitherEffect() {
        setDelirium();
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        castTitan();

        harness.handleListChoice(player1, DAMAGE_MODE);
        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void illegalDamageTargetPreventsBothModesFromResolving() {
        setDelirium();
        GrizzlyBears topCard = new GrizzlyBears();
        Forest otherCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, otherCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTitan();

        harness.handleListChoice(player1, DAMAGE_MODE);
        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void libraryModeRequiresOneCardAndPreservesUntouchedLibraryAboveTheRest() {
        GrizzlyBears chosen = new GrizzlyBears();
        Forest first = new Forest();
        Island second = new Island();
        GrizzlyBears third = new GrizzlyBears();
        Island untouched = new Island();
        harness.setLibrary(player1, List.of(chosen, first, second, third, untouched));
        castTitan();

        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                untouched, first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void singleCardLibraryPutsItsOnlyCardIntoHand() {
        Forest onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        castTitan();

        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoiceOrCauseADrawLoss() {
        harness.setLibrary(player1, List.of());
        castTitan();

        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void threeTypesAcrossFourCardsDoNotEnableBothModes() {
        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new GrizzlyBears(), new Forest(), new Island()));
        castTitan();

        harness.handleListChoice(player1, DAMAGE_MODE);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    private void castTitan() {
        harness.castFromHand(player1, new PropheticTitan(), "{4}{U}{R}");
        harness.passBothPriorities();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new SanctumWeaver(), new Forest()));
    }
}
