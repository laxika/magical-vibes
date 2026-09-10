package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundFootage.class, GrizzlyBears.class})
class FoundFootageTest extends BaseCardTest {

    @Test
    void controllerMayLookAtOpposingFaceDownCreatures() {
        harness.addToBattlefield(player1, new FoundFootage());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Grizzly Bears"));
    }

    @Test
    void sacrificeAbilitySurveilsTwoThenDrawsAndSacrificesFoundFootage() {
        Permanent foundFootage = harness.addToBattlefieldAndReturn(player1, new FoundFootage());
        Card surveilledCard = new GrizzlyBears();
        Card secondSurveilledCard = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(surveilledCard, secondSurveilledCard, drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(surveilledCard, secondSurveilledCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(foundFootage);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(surveilledCard, secondSurveilledCard, foundFootage.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
