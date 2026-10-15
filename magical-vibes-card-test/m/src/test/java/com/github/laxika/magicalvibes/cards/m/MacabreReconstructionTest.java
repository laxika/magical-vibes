package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RubblebeltMaverick;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MacabreReconstruction.class, GrizzlyBears.class, LeoninScimitar.class,
        RubblebeltMaverick.class, GravestoneStrider.class})
class MacabreReconstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target creature cards from the graveyard to hand")
    void returnsUpToTwoCreatureCards() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature1, creature2, artifact));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Costs two less after a creature card was put into your graveyard this turn")
    void costsTwoLessAfterCreatureCardWasPutIntoYourGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not reduce the cost for a creature card already in the graveyard")
    void doesNotReduceCostForCreatureCardAlreadyInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce the cost for a creature card put into an opponent's graveyard")
    void doesNotReduceCostForCreaturePutIntoOpponentsGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayChooseZeroTargetsEvenWhenCreaturesAreAvailable() {
        Card creature = new RubblebeltMaverick();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Macabre Reconstruction");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayCastWithAnEmptyGraveyard() {
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Macabre Reconstruction");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayChooseOnlyOneOfTwoAvailableCreatures() {
        Card chosen = new RubblebeltMaverick();
        Card unchosen = new RubblebeltMaverick();
        harness.setGraveyard(player1, List.of(chosen, unchosen));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherTargetIsExiled() {
        Card exiled = new RubblebeltMaverick();
        Card remaining = new RubblebeltMaverick();
        Card strider = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(exiled, remaining, strider));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId(), remaining.getId()));
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 2, 0, List.of(exiled.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(exiled, strider);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(exiled, strider, remaining);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void costsTwoLessAfterSurveillingACreatureIntoTheGraveyard() {
        Card milled = new RubblebeltMaverick();
        harness.setLibrary(player1, List.of(milled));
        harness.setHand(player1, List.of(new RubblebeltMaverick(), new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(milled.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceCostWhenOnlyANoncreatureCardEnteredTheGraveyard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsNothingWhenAllTargetsLeaveTheGraveyardBeforeResolution() {
        Card creature = new RubblebeltMaverick();
        Card strider = new GravestoneStrider();
        harness.setGraveyard(player1, List.of(creature, strider));
        harness.setHand(player1, List.of(new MacabreReconstruction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), strider.getId()));
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(creature, strider);
        harness.assertInGraveyard(player1, "Macabre Reconstruction");
        assertThat(gd.stack).isEmpty();
    }
}
