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

    @Test
    void canPutTopCardOnBottomWithoutLookingAtSecondCard() {
        Permanent witch = addReadyWitch(player1);
        Card topCard = new WarteyeWitch();
        Card nextCard = new WarteyeWitch();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        destroy(witch);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void simultaneousDeathsTriggerForSelfAndEachOtherControlledCreature() {
        Permanent firstWitch = addReadyWitch(player1);
        Permanent secondWitch = addReadyWitch(player1);
        Card topCard = new WarteyeWitch();
        harness.setLibrary(player1, List.of(topCard));
        firstWitch.setMarkedDamage(2);
        secondWitch.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        for (int i = 0; i < 4; i++) {
            resolveAllTriggers();
            assertScry(topCard);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void dyingWithEmptyLibraryFinishesWithoutPrompt() {
        Permanent witch = addReadyWitch(player1);
        harness.setLibrary(player1, List.of());

        destroy(witch);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exileDoesNotTriggerDeathAbility() {
        Permanent witch = addReadyWitch(player1);
        Card topCard = new WarteyeWitch();
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, witch));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotOnBattlefield(player1, "Warteye Witch");
    }

    @Test
    void controllerOfDyingWitchScriesTheirOwnLibrary() {
        Permanent witch = addReadyWitch(player2);
        Card topCard = new WarteyeWitch();
        Card otherPlayersCard = new WarteyeWitch();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(otherPlayersCard));

        destroy(witch);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherPlayersCard);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyWitch(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new WarteyeWitch());
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
