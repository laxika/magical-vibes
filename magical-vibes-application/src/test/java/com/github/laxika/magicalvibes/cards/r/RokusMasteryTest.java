package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RokusMastery.class, GrizzlyBears.class, HillGiant.class})
class RokusMasteryTest extends BaseCardTest {

    @Test
    void dealsXDamageToTargetCreatureWithoutScryingBelowFour() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void dealsXDamageAndScriesTwoAtFour() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new RokusMastery()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, 4, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
