package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonAppeasement.class, GrizzlyBears.class, Forest.class})
class DragonAppeasementTest extends BaseCardTest {

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new DragonAppeasement());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities(); // advance UPKEEP → DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Skipped draw step offers no priority before the main phase")
    void skippedDrawStepOffersNoPriority() {
        harness.addToBattlefield(player1, new DragonAppeasement());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Opponent still draws during their draw step")
    void opponentDoesNotSkipDrawStep() {
        harness.addToBattlefield(player1, new DragonAppeasement());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature sacrifice does not trigger the enchantment")
    void opponentSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new DragonAppeasement());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());

        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player2.getId(), bears.getCard()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a creature and accepting draws a card")
    void sacrificeCreatureAcceptDrawsCard() {
        harness.addToBattlefield(player1, new DragonAppeasement());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        sacrifice(bears);
        harness.passBothPriorities(); // resolve trigger → queue may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Sacrificing a creature and declining draws nothing")
    void sacrificeCreatureDeclineDrawsNothing() {
        harness.addToBattlefield(player1, new DragonAppeasement());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        sacrifice(bears);
        harness.passBothPriorities(); // resolve trigger → queue may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Sacrificing a non-creature permanent does not draw")
    void sacrificeNonCreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new DragonAppeasement());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        sacrifice(land);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    // Simulate player1 sacrificing one of their permanents, firing the
    // ON_ALLY_PERMANENT_SACRIFICED trigger through the engine's collection path.
    private void sacrifice(Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), card));
    }
}
