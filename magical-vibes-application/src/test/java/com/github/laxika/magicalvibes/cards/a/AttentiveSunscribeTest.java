package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttentiveSunscribe.class, GrizzlyBears.class})
class AttentiveSunscribeTest extends BaseCardTest {

    @Test
    @DisplayName("Attentive Sunscribe scries 1 when it becomes tapped")
    void becomingTappedTriggersScry() {
        Permanent sunscribe = addCreatureReady(player1, new AttentiveSunscribe());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Card originalTop = gd.playerDecks.get(player1.getId()).getFirst();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(originalTop);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(originalTop);
        assertThat(sunscribe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attentive Sunscribe does not trigger when another creature becomes tapped")
    void anotherCreatureBecomingTappedDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new AttentiveSunscribe());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
