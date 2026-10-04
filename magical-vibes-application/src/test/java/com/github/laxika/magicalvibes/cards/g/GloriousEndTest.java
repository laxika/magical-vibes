package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientCrab;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseGameAtEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloriousEnd.class, PlatinumAngel.class, AncientCrab.class, GeistOfSaintTraft.class})
class GloriousEndTest extends BaseCardTest {

    private void castGloriousEnd() {
        harness.setHand(player1, List.of(new GloriousEnd()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("Resolving ends the turn and registers a delayed 'lose the game'")
    void resolvingEndsTurnAndRegistersDelayedLoss() {
        int turnBefore = gd.turnNumber;
        castGloriousEnd();

        // "End the turn" empties the stack (including Glorious End itself).
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("The turn ends.")).isTrue();

        List<LoseGameAtEndStep> pending = gd.getDelayedActions(LoseGameAtEndStep.class);
        assertThat(pending).hasSize(1);
        assertThat(pending.getFirst().playerId()).isEqualTo(player1.getId());
        assertThat(pending.getFirst().registeredTurnNumber()).isEqualTo(turnBefore);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The intervening opponent's end step does not trigger the loss")
    void opponentEndStepDoesNotTriggerLoss() {
        castGloriousEnd();
        harness.passUntil(player2, TurnStep.END_STEP);

        // "your next end step" skips opponents' end steps: the loss stays scheduled.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
    }

    @Test
    @DisplayName("You lose the game at the beginning of your own next end step")
    void ownNextEndStepTriggersLoss() {
        castGloriousEnd();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities(); // resolve the loss

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the game"));
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Platinum Angel keeps you from losing at your next end step")
    void platinumAngelPreventsLoss() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        castGloriousEnd();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve the loss trigger

        // Can't-lose: the trigger resolves but the player stays in the game.
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void resolvingExilesItselfAndOpponentsSpell() {
        AncientCrab crab = new AncientCrab();
        GloriousEnd end = new GloriousEnd();
        harness.setHand(player2, List.of(crab));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setHand(player1, List.of(end));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(end);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(crab);
        harness.assertNotInGraveyard(player1, "Glorious End");
        harness.assertNotOnBattlefield(player2, "Ancient Crab");
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void exilingLossTriggerDoesNotMakeItTriggerAgain() {
        castGloriousEnd();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new GloriousEnd()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(1);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void damageRemainsUntilCleanupDiscardIsComplete() {
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new AncientCrab());
        crab.setMarkedDamage(1);
        List<Card> hand = new ArrayList<>();
        hand.add(new GloriousEnd());
        for (int i = 0; i < 8; i++) {
            hand.add(new AncientCrab());
        }
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(crab.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void skippedEndOfCombatPreservesUntriggeredTokenExile() {
        addCreatureReady(player1, new GeistOfSaintTraft());
        harness.setHand(player1, List.of(new GloriousEnd()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });
        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0);
        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, harness::passBothPriorities);

        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void endingTurnBeforeNextEndStepPostponesExistingLoss() {
        castGloriousEnd();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GloriousEnd()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(LoseGameAtEndStep.class)).hasSize(2);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void endingCombatRemovesCreaturesFromCombatAndClearsDamage() {
        Permanent crab = addCreatureReady(player1, new AncientCrab());
        harness.setHand(player1, List.of(new GloriousEnd()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(crab.isAttacking()).isTrue();
        crab.setMarkedDamage(1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(crab.isAttacking()).isFalse();
        assertThat(crab.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Ancient Crab");
    }
}
