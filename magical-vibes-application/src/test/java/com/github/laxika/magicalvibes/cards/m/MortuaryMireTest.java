package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

@CardUsed({MortuaryMire.class, BalefulStrix.class, SolRing.class})
class MortuaryMireTest extends BaseCardTest {

    @Test
    void entersTappedAndMayPutACreatureFromTheGraveyardOnTopOfTheLibrary() {
        Card creature = new BalefulStrix();
        Card nonCreature = new SolRing();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent mire = findPermanent(player1, "Mortuary Mire");
        assertThat(mire.isTapped()).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void canDeclineTheGraveyardReturn() {
        Card creature = new BalefulStrix();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void tapsForBlackMana() {
        Permanent mire = harness.addToBattlefieldAndReturn(player1, new MortuaryMire());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mire.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void cannotTargetACreatureInAnOpponentsGraveyard() {
        Card ownCreature = new BalefulStrix();
        Card opponentsCreature = new BalefulStrix();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
    }

    @Test
    void entersTappedWithoutACreatureInItsControllersGraveyard() {
        Card nonCreature = new SolRing();
        Card opponentsCreature = new BalefulStrix();
        Card libraryCard = new SolRing();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Mortuary Mire").isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
    }

    @Test
    void doesNotChooseAnotherCreatureWhenTheTargetLeavesTheGraveyard() {
        Card target = new BalefulStrix();
        Card otherCreature = new BalefulStrix();
        Card libraryCard = new SolRing();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void putsOnlyTheChosenCreatureAboveTheExistingLibrary() {
        Card target = new BalefulStrix();
        Card otherCreature = new BalefulStrix();
        Card topCard = new SolRing();
        Card bottomCard = new SolRing();
        harness.setGraveyard(player1, List.of(otherCreature, target));
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, topCard, bottomCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCreature);
    }
}
