package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrainedArynx.class, GrizzlyBears.class})
class TrainedArynxTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Trained Arynx")
    void saddleTapsAnotherCreature() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(arynx.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled grants first strike and triggers scry 1")
    void attacksWhileSaddled() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        arynx.setSaddled(true);

        Card originalTop = gd.playerDecks.get(player1.getId()).getFirst();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(originalTop);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attacking while not saddled does not grant first strike or scry")
    void doesNotTriggerWhenNotSaddled() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());

        declareAttackers(player1, List.of(0));
        arynx.setSaddled(true);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
