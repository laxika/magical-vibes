package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RevokePrivileges;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrainedArynx.class, RevokePrivileges.class})
class TrainedArynxTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Trained Arynx")
    void saddleTapsAnotherCreature() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());

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

    @Test
    @DisplayName("A creature that cannot crew Vehicles can still saddle")
    void creatureThatCannotCrewCanSaddle() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RevokePrivileges());
        aura.setAttachedTo(helper.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(arynx.isSaddled()).isTrue();
    }

    @Test
    @DisplayName("A summoning sick creature can pay the saddle cost")
    void summoningSickCreatureCanSaddle() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(arynx.isSaddled()).isTrue();
    }

    @Test
    @DisplayName("Trained Arynx cannot saddle itself")
    void cannotSaddleItself() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(arynx.isTapped()).isFalse();
        assertThat(arynx.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Saddle cannot be activated during combat")
    void cannotSaddleDuringCombat() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        Permanent helper = addCreatureReady(player1, new TrainedArynx());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(arynx.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom")
    void scryCanPutCardOnBottom() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        arynx.setSaddled(true);
        Card top = new TrainedArynx();
        Card next = new TrainedArynx();
        harness.setLibrary(player1, List.of(top, next));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, arynx, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An empty library does not prevent first strike")
    void emptyLibraryStillGrantsFirstStrike() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        arynx.setSaddled(true);
        harness.setLibrary(player1, List.of());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, arynx, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Saddled designation expires at end of turn")
    void saddleExpiresAtEndOfTurn() {
        Permanent arynx = addCreatureReady(player1, new TrainedArynx());
        addCreatureReady(player1, new TrainedArynx());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(arynx.isSaddled()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(arynx.isSaddled()).isFalse();
    }
}
