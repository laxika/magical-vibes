package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AethermagesTouch.class, Forest.class, GrizzlyBears.class, Shock.class})
class AethermagesTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a revealed creature onto the battlefield and returns it at your end step")
    void putsCreatureOntoBattlefieldAndReturnsItAtYourEndStep() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest rest1 = new Forest();
        Shock rest2 = new Shock();
        Shock rest3 = new Shock();
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
    @DisplayName("May decline the creature and put all revealed cards on the bottom")
    void mayDeclineCreature() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest rest1 = new Forest();
        Shock rest2 = new Shock();
        Shock rest3 = new Shock();
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
        Forest land = new Forest();
        Shock spell1 = new Shock();
        Shock spell2 = new Shock();
        Shock spell3 = new Shock();
        setLibrary(land, spell1, spell2, spell3);

        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, spell1, spell2, spell3);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new AethermagesTouch()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
