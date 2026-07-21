package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SenTripletsTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    /** Resolve Sen Triplets' upkeep trigger with {@code target} chosen; play1 controls Sen. */
    private void lockOpponent(Player target) {
        harness.addToBattlefield(player1, new SenTriplets());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

    // ===== Upkeep trigger targeting =====

    @Test
    @DisplayName("Upkeep trigger only offers opponents as valid targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new SenTriplets());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SenTriplets());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.senControlledPlayerId).isNull();
        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }

    // ===== Resolution locks the chosen opponent =====

    @Test
    @DisplayName("Resolving the trigger silences the opponent, blocks their abilities, and opens the sen window")
    void resolvingLocksTargetOpponent() {
        lockOpponent(player2);

        assertThat(gd.playersSilencedThisTurn).contains(player2.getId()).doesNotContain(player1.getId());
        assertThat(gd.playersCantActivateAbilitiesThisTurn).contains(player2.getId());
        assertThat(gd.senControllerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.senControlledPlayerId).isEqualTo(player2.getId());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("plays with their hand revealed"));
    }

    @Test
    @DisplayName("Locked opponent cannot cast spells")
    void lockedOpponentCannotCastSpells() {
        lockOpponent(player2);

        // Use an instant so the only reason it's uncastable is the lock (not sorcery-speed/off-turn).
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Locked opponent cannot activate abilities")
    void lockedOpponentCannotActivateAbilities() {
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyro = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prodigal Pyromancer"))
                .findFirst().orElseThrow();
        pyro.setSummoningSick(false);

        lockOpponent(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sen Triplets' controller can still cast their own spells")
    void controllerCanStillCast() {
        lockOpponent(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("The lock and sen window are cleared at end of turn")
    void locksClearedAtEndOfTurn() {
        gd.playersSilencedThisTurn.add(player2.getId());
        gd.playersCantActivateAbilitiesThisTurn.add(player2.getId());
        gd.senControllerPlayerId = player1.getId();
        gd.senControlledPlayerId = player2.getId();

        new TurnCleanupService(null).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersSilencedThisTurn).isEmpty();
        assertThat(gd.playersCantActivateAbilitiesThisTurn).isEmpty();
        assertThat(gd.senControllerPlayerId).isNull();
        assertThat(gd.senControlledPlayerId).isNull();
    }
}
