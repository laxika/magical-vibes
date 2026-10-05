package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Psychomancer.class, MindStone.class})
class PsychomancerTest extends BaseCardTest {

    @Test
    void triggersWhenItDies() {
        Permanent psychomancer = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToGraveyard(psychomancer);
        resolveTargetedTrigger();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void triggersWhenItIsExiled() {
        Permanent psychomancer = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToExile(psychomancer);
        resolveTargetedTrigger();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void triggersForAnotherNontokenArtifactYouControlGoingToGraveyardOrExile() {
        harness.addToBattlefield(player1, new Psychomancer());
        Permanent graveyardArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent exiledArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToGraveyard(graveyardArtifact);
        resolveTargetedTrigger();
        removeToExile(exiledArtifact);
        resolveTargetedTrigger();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    void ignoresTokensAndArtifactsControlledByOpponents() {
        harness.addToBattlefield(player1, new Psychomancer());
        Card tokenCard = new MindStone();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToGraveyard(token);
        removeToGraveyard(opponentArtifact);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }


    @Test
    void triggersForItsOwnDeathEvenWhenItIsAToken() {
        Card tokenCard = new Psychomancer();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToGraveyard(token);
        resolveTargetedTrigger();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void triggersForItsOwnExileEvenWhenItIsAToken() {
        Card tokenCard = new Psychomancer();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToExile(token);
        resolveTargetedTrigger();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void ignoresExiledTokensAndArtifactsControlledByOpponents() {
        harness.addToBattlefield(player1, new Psychomancer());
        Card tokenCard = new MindStone();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToExile(token);
        removeToExile(opponentArtifact);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerWhenItOrAnotherArtifactReturnsToHand() {
        Permanent psychomancer = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, artifact));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, psychomancer));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachPsychomancerSeesBothSimultaneousDeaths() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        while (gd.interaction.isAwaitingInput()) {
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).containsExactly(player2.getId());
            harness.handlePermanentChosen(player1, player2.getId());
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Psychomancer");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
    }

    @Test
    void faceDownPsychomancerDoesNotTriggerForAnotherArtifact() {
        Permanent psychomancer = harness.addToBattlefieldAndReturn(player1, new Psychomancer());
        psychomancer.setFaceDown(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        removeToGraveyard(artifact);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    private void resolveTargetedTrigger() {
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
    }
}
