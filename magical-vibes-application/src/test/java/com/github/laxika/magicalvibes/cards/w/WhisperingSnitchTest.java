package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperingSnitchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and gains life on the first surveil each turn")
    void triggersOnlyOnFirstSurveilEachTurn() {
        addCreatureReady(player1, new WhisperingSnitch());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<Card> library = List.of(new GrizzlyBears(), new Island(), new GrizzlyBears(), new Island());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DazzlingLights(), new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        harness.castInstant(player1, 0, secondTarget.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 1);
    }
}
