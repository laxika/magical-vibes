package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LodestoneBauble.class, Forest.class, Island.class, HillGiant.class, GrizzlyBears.class})
class LodestoneBaubleTest extends BaseCardTest {

    private Permanent addBauble() {
        Permanent bauble = addCreatureReady(player1, new LodestoneBauble());
        harness.addMana(player1, ManaColor.GREEN, 1);
        return bauble;
    }

    private int baubleIndex(Permanent bauble) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(bauble);
    }

    @Test
    @DisplayName("The controller chooses the lands' library order during resolution")
    void ownGraveyardLandsGoOnTopOfLibrary() {
        Permanent bauble = addBauble();

        Card forest = new Forest();
        Card island = new Island();
        harness.setGraveyard(player1, List.of(forest, island));
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId(), island.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        int forestIndex = reorder.cards().indexOf(forest);
        int islandIndex = reorder.cards().indexOf(island);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(islandIndex, forestIndex)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(0).getId()).isEqualTo(island.getId());
        assertThat(library.get(1).getId()).isEqualTo(forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(forest.getId(), island.getId());
    }

    @Test
    @DisplayName("The graveyard's owner draws a card at the beginning of the next turn's upkeep")
    void graveyardOwnerDrawsAtNextUpkeep() {
        Permanent bauble = addBauble();

        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.setLibrary(player2, List.of(new HillGiant(), new GrizzlyBears()));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0, List.of(forest.getId()));
        harness.passBothPriorities();

        // The card moved to its own owner's library, and the delayed draw belongs to that owner.
        assertThat(gd.playerDecks.get(player2.getId()).get(0).getId()).isEqualTo(forest.getId());
        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()).get(handBefore).getId()).isEqualTo(forest.getId());
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    void zeroTargetsStillSchedulesADraw() {
        Permanent bauble = addBauble();

        harness.setLibrary(player1, List.of(new HillGiant()));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bauble.getCard().getId());
    }

    @Test
    void offersBasicLandsFromEitherGraveyard() {
        Permanent bauble = addBauble();

        Card ownForest = new Forest();
        Card opponentIsland = new Island();
        Card nonBasic = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownForest, nonBasic));
        harness.setGraveyard(player2, List.of(opponentIsland));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, bauble.getCard(), bauble.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), baubleIndex(bauble));

        assertThat(response.validGraveyardCardIds())
                .containsExactlyInAnyOrder(ownForest.getId(), opponentIsland.getId());
    }

    @Test
    void reportsOptionalTargetRange() {
        Permanent bauble = addBauble();

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, bauble.getCard(), bauble.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), baubleIndex(bauble));

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.minTargets()).isZero();
        softly.assertThat(response.maxTargets()).isEqualTo(4);
        softly.assertAll();
    }

    @Test
    @DisplayName("Only basic land cards are legal targets")
    void rejectsNonBasicLandTarget() {
        Permanent bauble = addBauble();

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        int index = baubleIndex(bauble);
        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("All targets must come from a single player's graveyard")
    void rejectsTargetsSpanningTwoGraveyards() {
        Permanent bauble = addBauble();

        Card mine = new Forest();
        Card theirs = new Island();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));

        int index = baubleIndex(bauble);
        List<UUID> targets = List.of(mine.getId(), theirs.getId());
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At most four cards may be targeted")
    void rejectsMoreThanFourTargets() {
        Permanent bauble = addBauble();

        List<Card> lands = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Island());
        harness.setGraveyard(player1, lands);

        int index = baubleIndex(bauble);
        List<UUID> targets = lands.stream().map(Card::getId).toList();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingLegalTargetIsReturnedAndItsOwnerStillDraws() {
        Permanent bauble = addBauble();
        Card forest = new Forest();
        Card island = new Island();
        harness.setGraveyard(player2, List.of(forest, island));
        harness.setLibrary(player2, List.of(new HillGiant()));

        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId(), island.getId()));
        harness.setGraveyard(player2, List.of(island));
        harness.setExile(player2, List.of(forest));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).startsWith(island).doesNotContain(forest);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class).getFirst().controllerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void allTargetsLeavingTheGraveyardPreventsTheDelayedDraw() {
        Permanent bauble = addBauble();
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(forest));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bauble.getCard());
    }

    @Test
    void activatorControlsTheDelayedTriggerEvenWhenOpponentDraws() {
        Permanent bauble = addBauble();
        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));
        harness.setLibrary(player2, List.of(new HillGiant()));
        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId()));
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1).contains(forest);
    }

    @Test
    void ownGraveyardDrawOccursOnOpponentsUpkeepAndOnlyOnce() {
        Permanent bauble = addBauble();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));
        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId()));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(forest);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void controllerChoosesTheOrderInOpponentsLibrary() {
        Permanent bauble = addBauble();
        Card forest = new Forest();
        Card island = new Island();
        harness.setGraveyard(player2, List.of(forest, island));
        harness.setLibrary(player2, List.of(new HillGiant()));
        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                List.of(forest.getId(), island.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(forest), reorder.cards().indexOf(island))));

        assertThat(gd.playerDecks.get(player2.getId())).startsWith(forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest, island);
    }

    @Test
    void canTargetFourBasicLandsAndPaysCostsImmediately() {
        Permanent bauble = addBauble();
        List<Card> lands = List.of(new Forest(), new Forest(), new Island(), new Island());
        harness.setGraveyard(player1, lands);
        harness.activateAbilityWithGraveyardTargets(player1, baubleIndex(bauble), 0,
                lands.stream().map(Card::getId).toList());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bauble);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bauble.getCard()).containsAll(lands);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactlyInAnyOrderElementsOf(lands);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));
        assertThat(gd.playerDecks.get(player1.getId())).containsAll(lands);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent bauble = addBauble();
        bauble.setTapped(true);
        int index = baubleIndex(bauble);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bauble);
    }

    @Test
    void cannotActivateWithoutOneMana() {
        Permanent bauble = addCreatureReady(player1, new LodestoneBauble());
        int index = baubleIndex(bauble);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bauble);
    }
}
