package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriosityCrafter.class, GrizzlyBears.class})
class CuriosityCrafterTest extends BaseCardTest {

    @Test
    @DisplayName("A creature token dealing combat damage draws a card")
    void tokenCombatDamageDrawsCard() {
        addCuriosityCrafter();
        addReadyToken();
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A nontoken creature dealing combat damage does not draw a card")
    void nontokenCombatDamageDoesNotDrawCard() {
        addCuriosityCrafter();
        addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Each token dealing combat damage draws a separate card")
    void eachTokenTriggersSeparately() {
        addCuriosityCrafter();
        addReadyToken();
        addReadyToken();
        seedLibrary(2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        addCuriosityCrafter();
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opposing creature token does not trigger the draw ability")
    void opposingTokenDoesNotDrawCard() {
        addCuriosityCrafter();
        Card token = new GrizzlyBears();
        token.setToken(true);
        addCreatureReady(player2, token);
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not draw a card")
    void blockedTokenDoesNotDrawCard() {
        addCuriosityCrafter();
        addReadyToken();
        addCreatureReady(player2, new GrizzlyBears());
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Each Curiosity Crafter triggers for the same token")
    void multipleCraftersEachDrawCard() {
        addCuriosityCrafter();
        addCuriosityCrafter();
        addReadyToken();
        seedLibrary(2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("A draw trigger resolves after Curiosity Crafter leaves the battlefield")
    void drawTriggerSurvivesSourceLeaving() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        Permanent crafter = addCreatureReady(player1, new CuriosityCrafter());
        addReadyToken();
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(crafter);
        gd.playerGraveyards.get(player1.getId()).add(crafter.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Curiosity Crafter does not remove the opponent's hand size limit")
    void opponentStillDiscardsDuringCleanup() {
        addCuriosityCrafter();
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
    }

    private void addCuriosityCrafter() {
        addCreatureReady(player1, new CuriosityCrafter());
    }

    private Permanent addReadyToken() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return addCreatureReady(player1, tokenCard);
    }

    private void seedLibrary(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, cards);
    }
}
