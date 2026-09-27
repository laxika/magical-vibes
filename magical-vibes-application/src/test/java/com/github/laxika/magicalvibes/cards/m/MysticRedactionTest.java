package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticRedaction.class, Censor.class, GrizzlyBears.class})
class MysticRedactionTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 1 at the beginning of its controller's upkeep")
    void scriesAtBeginningOfUpkeep() {
        Card top = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addToBattlefield(player1, new MysticRedaction());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Each opponent mills two cards when its controller discards")
    void eachOpponentMillsTwoOnControllerDiscard() {
        harness.addToBattlefield(player1, new MysticRedaction());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
