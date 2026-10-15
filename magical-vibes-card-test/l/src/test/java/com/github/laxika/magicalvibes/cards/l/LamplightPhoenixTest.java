package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LamplightPhoenix.class, GrizzlyBears.class})
class LamplightPhoenixTest extends BaseCardTest {

    @Test
    void mayExileItselfCollectEvidenceAndReturnTapped() {
        Card evidenceOne = new GrizzlyBears();
        Card evidenceTwo = new GrizzlyBears();
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new LamplightPhoenix());
        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo));
        phoenix.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(evidenceOne.getId(), evidenceTwo.getId());
        harness.handleMultipleCardsChosen(player1, List.of(evidenceOne.getId(), evidenceTwo.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(phoenix.getCard().getId()))
                .singleElement()
                .satisfies(returned -> assertThat(returned.isTapped()).isTrue());
    }

    @Test
    void decliningLeavesItInTheGraveyard() {
        Card evidenceOne = new GrizzlyBears();
        Card evidenceTwo = new GrizzlyBears();
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new LamplightPhoenix());
        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo));
        phoenix.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(evidenceOne.getId(), evidenceTwo.getId(), phoenix.getCard().getId());
        harness.assertNotOnBattlefield(player1, "Lamplight Phoenix");
    }

    @Test
    void cannotUseItselfWhenThereIsNotEnoughOtherEvidence() {
        Card evidence = new GrizzlyBears();
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new LamplightPhoenix());
        harness.setGraveyard(player1, List.of(evidence));
        phoenix.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(evidence.getId(), phoenix.getCard().getId());
        harness.assertNotOnBattlefield(player1, "Lamplight Phoenix");
    }

    @Test
    void canCollectMoreThanFourManaValueAndLeaveUnselectedEvidence() {
        Card evidenceOne = new LamplightPhoenix();
        Card evidenceTwo = new LamplightPhoenix();
        Card unselected = new LamplightPhoenix();
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new LamplightPhoenix());
        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo, unselected));
        phoenix.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(phoenix.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(evidenceOne.getId(), evidenceTwo.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(unselected.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(evidenceOne.getId(), evidenceTwo.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(phoenix.getCard().getId()))
                .singleElement().satisfies(returned -> assertThat(returned.isTapped()).isTrue());
    }

    @Test
    void originalDeathTriggerCannotReturnPhoenixAfterItLeavesAndDiesAgain() {
        Card evidenceOne = new LamplightPhoenix();
        Card evidenceTwo = new LamplightPhoenix();
        Card phoenixCard = new LamplightPhoenix();
        Permanent first = harness.addToBattlefieldAndReturn(player1, phoenixCard);
        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo));
        first.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.setGraveyard(player1, List.of(evidenceOne, evidenceTwo));
        Permanent returned = harness.enterBattlefieldAndReturn(player1, phoenixCard);
        returned.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Lamplight Phoenix");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(evidenceOne.getId(), evidenceTwo.getId(), phoenixCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
