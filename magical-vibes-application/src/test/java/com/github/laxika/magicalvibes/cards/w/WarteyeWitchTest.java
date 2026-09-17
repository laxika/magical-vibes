package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarteyeWitch.class, GrizzlyBears.class})
class WarteyeWitchTest extends BaseCardTest {

    @Test
    void anotherCreatureYouControlDyingCausesScry() {
        addReadyWitch(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        destroy(bears);

        assertScry(topCard);
    }

    @Test
    void thisCreatureDyingCausesScry() {
        Permanent witch = addReadyWitch(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        destroy(witch);

        assertScry(topCard);
    }

    @Test
    void opponentCreatureDyingDoesNotCauseScry() {
        addReadyWitch(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        destroy(bears);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    private Permanent addReadyWitch(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new WarteyeWitch());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, permanent));
        harness.passBothPriorities();
    }

    private void assertScry(Card topCard) {
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
