package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SecretTunnel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCaveOfTwoLovers.class, Mountain.class, SecretTunnel.class})
class TheCaveOfTwoLoversTest extends BaseCardTest {

    @Test
    void chapterICreatesTwoAllyTokens() {
        addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
    }

    @Test
    void chapterIISearchesForMountainOrCave() {
        addSaga(1);
        Mountain mountain = new Mountain();
        Card cave = new SecretTunnel();
        harness.setLibrary(player1, List.of(new TheCaveOfTwoLovers(), mountain, cave));

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(mountain, cave);
        harness.handleCardChosen(player1, search.params().cards().indexOf(cave));

        assertThat(gd.playerHands.get(player1.getId())).contains(cave);
    }

    @Test
    void chapterIIIEarthbendsALandYouControl() {
        addSaga(2);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, mountain)).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.HASTE)).isTrue();
        assertThat(mountain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void chapterIIIEarthbendRejectsALandControlledByOpponent() {
        addSaga(2);
        Permanent ownMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, ownMountain.getId());
        harness.passBothPriorities();
    }

    @Test
    void chapterIICanFindMountain() {
        addSaga(1);
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain));

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(mountain);
    }

    @Test
    void chapterIICanFailToFindEvenWithMatchingCard() {
        addSaga(1);
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain));

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIWithNoMatchingCardsFinishesWithoutAddingToHand() {
        addSaga(1);
        Card nonmatchingCard = new TheCaveOfTwoLovers();
        harness.setLibrary(player1, List.of(nonmatchingCard));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonmatchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void earthbendAddsCountersToExistingCountersAndPreservesTappedState() {
        addSaga(2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        land.tap();

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
        assertThat(land.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "The Cave of Two Lovers");
    }

    @Test
    void dyingEarthbendedLandReturnsAfterSagaIsSacrificed() {
        Permanent land = earthbendMountain();
        harness.assertNotOnBattlefield(player1, "The Cave of Two Lovers");
        harness.assertInGraveyard(player1, "The Cave of Two Lovers");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, land));
        harness.assertInGraveyard(player1, "Mountain");
        harness.passBothPriorities();

        assertReturnedMountain(land);
        harness.assertNotInGraveyard(player1, "Mountain");
    }

    @Test
    void exiledEarthbendedLandReturnsTapped() {
        Permanent land = earthbendMountain();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.passBothPriorities();

        assertReturnedMountain(land);
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    void bouncedEarthbendedLandStaysInHand() {
        Permanent land = earthbendMountain();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, land));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    void enteringSagaTriggersFirstChapter() {
        harness.setHand(player1, List.of(new TheCaveOfTwoLovers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
        harness.assertOnBattlefield(player1, "The Cave of Two Lovers");
    }

    @Test
    void chapterIIIRejectsNonlandPermanent() {
        Permanent saga = addSaga(2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());

        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, saga.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
    }
    private Permanent earthbendMountain() {
        addSaga(2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        return land;
    }

    private void assertReturnedMountain(Permanent original) {
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Mountain"));
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCard().getId()).isEqualTo(original.getCard().getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCaveOfTwoLovers());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
