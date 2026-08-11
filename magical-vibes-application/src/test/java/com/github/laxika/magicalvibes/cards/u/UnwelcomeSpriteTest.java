package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnwelcomeSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell during an opponent's turn triggers surveil 2")
    void castsSpellDuringOpponentsTurnSurveilsTwo() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, top1);
        gd.playerDecks.get(player1.getId()).add(0, top0);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passPriority(player2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1);
    }

    @Test
    @DisplayName("Casting a spell during your own turn does not trigger surveil")
    void castsSpellDuringOwnTurnDoesNotSurveil() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new UnwelcomeSprite());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }
}
