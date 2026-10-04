package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.p.PleaForPower;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErestorOfTheCouncil.class, PleaForPower.class, CommandTower.class})
class ErestorOfTheCouncilTest extends BaseCardTest {

    @Test
    void sharedVoteCreatesTreasureAndDrawsACard() {
        CommandTower drawnCard = new CommandTower();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.enterBattlefieldAndReturn(player1, new ErestorOfTheCouncil());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.TIME);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void differentVoteCausesScryBeforeDrawing() {
        Card first = new CommandTower();
        Card second = new CommandTower();
        Card third = new CommandTower();
        Card fourth = new CommandTower();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.enterBattlefieldAndReturn(player1, new ErestorOfTheCouncil());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }


    @Test
    void differentVoteCanBottomACardBeforeTheTriggeredDraw() {
        Card first = new CommandTower();
        Card second = new CommandTower();
        Card third = new CommandTower();
        Card bottomed = new CommandTower();
        Card drawn = new CommandTower();
        harness.setLibrary(player1, List.of(first, second, third, bottomed, drawn));
        harness.enterBattlefieldAndReturn(player1, new ErestorOfTheCouncil());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(bottomed);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third, drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void matchingVoteUsesErestorsControllerWhenOpponentCastsTheVotingSpell() {
        Card drawn = new CommandTower();
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player2, new ErestorOfTheCouncil());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.TIME);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castPleaForPower() {
        harness.castFromHand(player1, new PleaForPower(), "{3}{U}");
        harness.passBothPriorities();
    }
}
