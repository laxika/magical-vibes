package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvenPassage.class, Forest.class, Plains.class, LlanowarElves.class, GrizzlyBears.class})
class ElvenPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Elven Passage pays 1 life and sacrifices it")
    void activationPaysLifeAndSacrificesIt() {
        ElvenPassage passage = new ElvenPassage();
        harness.addToBattlefield(player1, passage);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == passage);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(passage);
    }

    @Test
    @DisplayName("The ability searches for a basic land tapped")
    void searchesForBasicLandTapped() {
        Forest forest = activatePassage();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).contains(forest).hasSize(2);
        assertThat(search.params().cards()).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Beholding an Elf you control untaps the fetched land")
    void beholdControlledElfUntapsFetchedLand() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Forest forest = activatePassage();

        chooseFetchedLand();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(elf.getCard().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest)
                .singleElement()
                .matches(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Declining to behold leaves the fetched land tapped")
    void declineBeholdLeavesFetchedLandTapped() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Forest forest = activatePassage();

        chooseFetchedLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest)
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("No Elf means there is no behold prompt")
    void noElfSkipsBeholdPrompt() {
        Forest forest = activatePassage();

        chooseFetchedLand();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest)
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("An Elf card in hand can be beheld")
    void beholdElfCardInHandUntapsFetchedLand() {
        Card elf = new LlanowarElves();
        harness.setHand(player1, List.of(elf));
        Forest forest = activatePassage();

        chooseFetchedLand();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(elf.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == forest)
                .singleElement()
                .matches(permanent -> !permanent.isTapped());
    }

    private Forest activatePassage() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new ElvenPassage());
        harness.setLibrary(player1, List.of(forest, new Plains(), new GrizzlyBears()));
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int passageIndex = 0;
        while (!(battlefield.get(passageIndex).getCard() instanceof ElvenPassage)) {
            passageIndex++;
        }
        harness.activateAbility(player1, passageIndex, null, null);
        return forest;
    }

    private void chooseFetchedLand() {
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
    }
}
