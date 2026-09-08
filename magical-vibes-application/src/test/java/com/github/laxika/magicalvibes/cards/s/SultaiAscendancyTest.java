package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SultaiAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, surveils 2")
    void surveilsTwoAtBeginningOfOwnUpkeep() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new SultaiAscendancy());
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, top1);
        gd.playerDecks.get(player1.getId()).add(0, top0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top0, top1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top0, top1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player1, new SultaiAscendancy());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
