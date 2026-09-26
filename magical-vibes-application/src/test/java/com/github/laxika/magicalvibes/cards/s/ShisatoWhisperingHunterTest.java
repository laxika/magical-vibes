package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.o.OrochiSustainer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShisatoWhisperingHunter.class, HumbleBudoka.class, OrochiSustainer.class})
class ShisatoWhisperingHunterTest extends BaseCardTest {

    // "At the beginning of your upkeep, sacrifice a Snake."
    // "Whenever Shisato deals combat damage to a player, that player skips their next untap step."

    @Test
    @DisplayName("Combat damage to a player makes that player skip their next untap step")
    void combatDamageSkipsDamagedPlayersUntapStep() {
        Permanent shisato = addCreatureReady(player1, new ShisatoWhisperingHunter());
        shisato.setAttacking(true);
        Permanent enemyBudoka = addCreatureReady(player2, new HumbleBudoka());
        enemyBudoka.tap();

        resolveCombat();
        harness.passBothPriorities(); // resolve the skip-untap trigger
        harness.assertLife(player2, 18);

        endTurn(); // player 2's turn — their untap step is skipped

        assertThat(enemyBudoka.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only one untap step is skipped — the following one untaps normally")
    void skipsOnlyTheNextUntapStep() {
        Permanent shisato = addCreatureReady(player1, new ShisatoWhisperingHunter());
        shisato.setAttacking(true);
        Permanent enemyBudoka = addCreatureReady(player2, new HumbleBudoka());
        enemyBudoka.tap();

        resolveCombat();
        harness.passBothPriorities();

        endTurn(); // player 2's turn — untap step skipped
        assertThat(enemyBudoka.isTapped()).isTrue();

        endTurn(); // back to player 1
        endTurn(); // player 2's next turn — untaps normally
        assertThat(enemyBudoka.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A blocked Shisato deals no damage to the player, so no untap step is skipped")
    void blockedShisatoSkipsNothing() {
        Permanent shisato = addCreatureReady(player1, new ShisatoWhisperingHunter());
        shisato.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HumbleBudoka());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent enemyBudoka = addCreatureReady(player2, new HumbleBudoka());
        enemyBudoka.tap();

        resolveCombat();

        endTurn(); // player 2 untaps normally

        assertThat(enemyBudoka.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With itself the only Snake, the upkeep trigger sacrifices Shisato")
    void upkeepSacrificesItselfWhenOnlySnake() {
        harness.addToBattlefield(player1, new ShisatoWhisperingHunter());
        harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shisato, Whispering Hunter");
        harness.assertOnBattlefield(player1, "Humble Budoka");
    }

    @Test
    @DisplayName("With another Snake around, the controller chooses which Snake to sacrifice")
    void upkeepControllerChoosesWhichSnake() {
        Permanent shisato = harness.addToBattlefieldAndReturn(player1, new ShisatoWhisperingHunter());
        Permanent snake = harness.addToBattlefieldAndReturn(player1, new OrochiSustainer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).contains(shisato.getId(), snake.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(snake.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(shisato.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(snake.getId()));
    }

    @Test
    @DisplayName("The upkeep trigger does not fire during an opponent's upkeep")
    void upkeepDoesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new ShisatoWhisperingHunter());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shisato, Whispering Hunter");
    }

    /** Ends the current turn; the other player becomes active and takes their untap step. */
    private void endTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        var nextPlayer = gd.activePlayerId.equals(player1.getId()) ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(nextPlayer, TurnStep.UPKEEP);
    }
}
