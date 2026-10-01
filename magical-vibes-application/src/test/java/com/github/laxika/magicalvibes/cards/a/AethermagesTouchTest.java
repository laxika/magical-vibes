package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CytoplastManipulator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AethermagesTouch.class, AssaultZeppelid.class, AzoriusSignet.class, CytoplastManipulator.class})
class AethermagesTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a revealed creature onto the battlefield and returns it at your end step")
    void putsCreatureOntoBattlefieldAndReturnsItAtYourEndStep() {
        AssaultZeppelid creature = new AssaultZeppelid();
        AzoriusSignet rest1 = new AzoriusSignet();
        AzoriusSignet rest2 = new AzoriusSignet();
        AzoriusSignet rest3 = new AzoriusSignet();
        setLibrary(creature, rest1, rest2, rest3);

        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rest1, rest2, rest3);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(entered.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP
                        && player1.getId().equals(action.controllerId()));

        advanceToEndStep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));

        advanceToEndStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Puts at most one creature onto the battlefield when multiple are revealed")
    void putsAtMostOneCreatureOntoBattlefield() {
        AssaultZeppelid firstCreature = new AssaultZeppelid();
        AssaultZeppelid secondCreature = new AssaultZeppelid();
        AzoriusSignet rest1 = new AzoriusSignet();
        AzoriusSignet rest2 = new AzoriusSignet();
        setLibrary(firstCreature, secondCreature, rest1, rest2);

        castAndResolve();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(secondCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firstCreature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(firstCreature, rest1, rest2);
    }

    @Test
    @DisplayName("Returns the creature at the end step of its current controller")
    void returnsAtCurrentControllersEndStepAfterControlChanges() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new CytoplastManipulator(), "{2}{U}{U}");
        harness.passBothPriorities();
        Permanent manipulator = findPermanent(player2, "Cytoplast Manipulator");
        manipulator.setSummoningSick(false);

        CytoplastManipulator chosenCreature = new CytoplastManipulator();
        AzoriusSignet rest1 = new AzoriusSignet();
        AzoriusSignet rest2 = new AzoriusSignet();
        AzoriusSignet rest3 = new AzoriusSignet();
        setLibrary(chosenCreature, rest1, rest2, rest3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castAndResolve();
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chosenCreature.getId()))
                .findFirst()
                .orElseThrow();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(manipulator), null,
                entered.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(entered.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(entered.getCard().getId()));

        advanceToEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(entered.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosenCreature);

        advanceToEndStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(entered.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCreature);
    }

    @Test
    @DisplayName("May decline the creature and put all revealed cards on the bottom")
    void mayDeclineCreature() {
        AssaultZeppelid creature = new AssaultZeppelid();
        AzoriusSignet rest1 = new AzoriusSignet();
        AzoriusSignet rest2 = new AzoriusSignet();
        AzoriusSignet rest3 = new AzoriusSignet();
        setLibrary(creature, rest1, rest2, rest3);

        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, rest1, rest2, rest3);
    }

    @Test
    @DisplayName("Puts all revealed cards on the bottom when no creature is revealed")
    void putsAllCardsOnBottomWithoutCreature() {
        AzoriusSignet artifact1 = new AzoriusSignet();
        AzoriusSignet artifact2 = new AzoriusSignet();
        AzoriusSignet artifact3 = new AzoriusSignet();
        AzoriusSignet artifact4 = new AzoriusSignet();
        setLibrary(artifact1, artifact2, artifact3, artifact4);

        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(artifact1, artifact2, artifact3, artifact4);
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new AethermagesTouch(), "{2}{W}{U}");
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
