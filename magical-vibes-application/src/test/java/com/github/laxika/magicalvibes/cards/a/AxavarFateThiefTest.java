package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AxavarFateThief.class, Forest.class, GrizzlyBears.class})
class AxavarFateThiefTest extends BaseCardTest {

    @Test
    void voidDiscardsThenHeistsThreeRandomNonlandCards() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        Permanent leavingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leavingCreature));

        Card discarded = new Forest();
        Card land = new Forest();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player2, List.of(land, first, second, third));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3)
                .allMatch(card -> !card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND));

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(chosen.getId());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(chosen);
    }

    @Test
    void doesNotTriggerWithoutVoid() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        enterEndStep();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landLeavingDoesNotEnableVoid() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        enterEndStep();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        enableVoid();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stillHeistsWithEmptyHandAndOnlyOneNonlandCard() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        enableVoid();
        Card stolen = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(land, stolen));
        enterEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(stolen);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(stolen);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(stolen.getId())
                && entry.faceDown());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolen.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(stolen);
    }

    @Test
    void discardsEvenWhenLibraryContainsOnlyLands() {
        harness.addToBattlefield(player1, new AxavarFateThief());
        enableVoid();
        Card discarded = new Forest();
        Card land = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player2, List.of(land));
        enterEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void warpCanBePaidWithBlackMana() {
        castForWarpCost(ManaColor.BLACK);
    }

    @Test
    void warpCanBePaidWithRedMana() {
        castForWarpCost(ManaColor.RED);
    }

    private void castForWarpCost(ManaColor color) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card axavar = new AxavarFateThief();
        harness.setHand(player1, List.of(axavar));
        harness.addMana(player1, color, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(axavar.getId()));
        enterEndStep();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(axavar.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(axavar.getId())
                && player2.getId().equals(entry.getTargetId()));
    }

    private void enableVoid() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
    }

    private void enterEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }
}
