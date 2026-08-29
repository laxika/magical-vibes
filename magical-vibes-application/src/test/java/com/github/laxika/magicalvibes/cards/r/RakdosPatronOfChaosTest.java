package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosPatronOfChaos.class, GrizzlyBears.class, Forest.class})
class RakdosPatronOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Targeted opponent chooses two nonland, nontoken permanents to sacrifice")
    void opponentChoosesTwoMatchingPermanents() {
        harness.addToBattlefield(player1, new RakdosPatronOfChaos());
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent bear3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);

        resolveTriggerToOpponentChoice(player2);

        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(choice.validIds()).containsExactly(bear1.getId(), bear2.getId(), bear3.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(bear1.getId(), bear2.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(bear3.getId(), forest.getId(), token.getId());
    }

    @Test
    @DisplayName("If the opponent declines, Rakdos's controller draws two cards")
    void opponentDeclinesAndControllerDrawsTwo() {
        harness.addToBattlefield(player1, new RakdosPatronOfChaos());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest drawn1 = new Forest();
        Forest drawn2 = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn1, drawn2));

        resolveTriggerToOpponentChoice(player2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2)
                .containsExactly(drawn1, drawn2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(bear.getId(), otherBear.getId());
    }

    @Test
    @DisplayName("If the opponent cannot sacrifice two matching permanents, the controller draws two cards")
    void opponentHasFewerThanTwoMatchingPermanents() {
        harness.addToBattlefield(player1, new RakdosPatronOfChaos());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest drawn1 = new Forest();
        Forest drawn2 = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn1, drawn2));

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn1, drawn2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(bear.getId());
    }

    @Test
    @DisplayName("The ability triggers only during Rakdos's controller's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new RakdosPatronOfChaos());

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveTriggerToOpponentChoice(Player target) {
        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(target.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
