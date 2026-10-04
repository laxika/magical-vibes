package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForebodingSteamboat.class, GrizzlyBears.class})
class ForebodingSteamboatTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesUpToTwoOwnCreatures() {
        List<Permanent> player1Bears = addBears(player1, 3);
        List<Permanent> player2Bears = addBears(player2, 2);
        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.validIds()).containsExactlyInAnyOrderElementsOf(
                player1Bears.stream().map(Permanent::getId).toList());
        assertThat(player1Choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(player1Bears.get(0).getId(), player1Bears.get(1).getId()));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.validIds()).containsExactlyInAnyOrderElementsOf(
                player2Bears.stream().map(Permanent::getId).toList());
        harness.handleMultiplePermanentsChosen(player2,
                player2Bears.stream().map(Permanent::getId).toList());

        Permanent steamboat = findSteamboat(card);
        assertThat(gd.getCardsExiledByPermanent(steamboat.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Bears.get(2), steamboat);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void exiledCreaturesReturnWhenSteamboatLeaves() {
        List<Permanent> player1Bears = addBears(player1, 2);
        List<Permanent> player2Bears = addBears(player2, 2);
        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                player1Bears.stream().map(Permanent::getId).toList());
        harness.handleMultiplePermanentsChosen(player2,
                player2Bears.stream().map(Permanent::getId).toList());

        Permanent steamboat = findSteamboat(card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, steamboat));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getOriginalCard().getId())
                .containsExactlyInAnyOrderElementsOf(player1Bears.stream()
                        .map(permanent -> permanent.getOriginalCard().getId()).toList());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getOriginalCard().getId())
                .containsExactlyInAnyOrderElementsOf(player2Bears.stream()
                        .map(permanent -> permanent.getOriginalCard().getId()).toList());
        assertThat(gd.getCardsExiledByPermanent(steamboat.getId())).isEmpty();
    }

    @Test
    void attackPutsAnExiledCardIntoItsOwnersGraveyardAndInvestigates() {
        List<Permanent> player1Bears = addBears(player1, 2);
        List<Permanent> player2Bears = addBears(player2, 2);
        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                player1Bears.stream().map(Permanent::getId).toList());
        harness.handleMultiplePermanentsChosen(player2,
                player2Bears.stream().map(Permanent::getId).toList());

        Permanent steamboat = findSteamboat(card);
        Card cardToPutIntoGraveyard = gd.getCardsExiledByPermanent(steamboat.getId()).stream()
                .filter(exiled -> gd.findExiledCard(exiled.getId()).ownerId().equals(player2.getId()))
                .findFirst().orElseThrow();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(steamboat), null, null);
        harness.passBothPriorities();
        assertThat(crew.isTapped()).isTrue();

        steamboat.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(steamboat)));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, cardToPutIntoGraveyard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(cardToPutIntoGraveyard.getId())).isNull();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(steamboat.getId())).hasSize(3);
    }

    @Test
    void playerWithOnlyOneEligibleCreatureMustExileIt() {
        Permanent bear = addBears(player2, 1).getFirst();
        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(bear.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(bear.getId()));

        assertThat(gd.getCardsExiledByPermanent(findSteamboat(card).getId()))
                .containsExactly(bear.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void playerCannotChooseFewerThanTwoWhenTwoAreAvailable() {
        List<Permanent> bears = addBears(player1, 2);
        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(bears.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1,
                bears.stream().map(Permanent::getId).toList());

        assertThat(gd.getCardsExiledByPermanent(findSteamboat(card).getId())).hasSize(2);
    }

    @Test
    void tokenCreaturesAndCrewedVehiclesAreNotEligible() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent oldSteamboat = harness.addToBattlefieldAndReturn(player1, new ForebodingSteamboat());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oldSteamboat), null, null);
        harness.passBothPriorities();
        GrizzlyBears tokenCopy = new GrizzlyBears();
        tokenCopy.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCopy);

        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bear.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(oldSteamboat, token, findSteamboat(card));
        assertThat(gd.getCardsExiledByPermanent(findSteamboat(card).getId()))
                .containsExactly(bear.getOriginalCard());
    }

    @Test
    void sourceLeavingBeforeEntryTriggerResolvesDoesNotExileCreatures() {
        List<Permanent> bears = addBears(player2, 2);
        ForebodingSteamboat card = castSteamboat();
        Permanent steamboat = findSteamboat(card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, steamboat));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyElementsOf(bears);
        assertThat(gd.getCardsExiledByPermanent(steamboat.getId())).isEmpty();
    }

    @Test
    void attackWithNoExiledCardsDoesNotInvestigate() {
        ForebodingSteamboat card = castSteamboat();
        harness.passBothPriorities();
        Permanent steamboat = findSteamboat(card);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(steamboat), null, null);
        harness.passBothPriorities();
        steamboat.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(steamboat)));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private ForebodingSteamboat castSteamboat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        ForebodingSteamboat card = new ForebodingSteamboat();
        harness.castFromHand(player1, card, "{3}{B}{B}");
        harness.passBothPriorities();
        return card;
    }

    private Permanent findSteamboat(ForebodingSteamboat card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst().orElseThrow();
    }

    private List<Permanent> addBears(com.github.laxika.magicalvibes.model.Player player, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> harness.addToBattlefieldAndReturn(player, new GrizzlyBears()))
                .toList();
    }
}
