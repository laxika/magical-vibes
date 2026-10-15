package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SenTriplets.class, Shock.class, ProdigalPyromancer.class, Forest.class})
class SenTripletsTest extends BaseCardTest {

    /** Resolve Sen Triplets' upkeep trigger with {@code target} chosen; play1 controls Sen. */
    private void lockOpponent(Player target) {
        harness.addToBattlefield(player1, new SenTriplets());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

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
        Permanent pyro = findPermanent(player2, "Prodigal Pyromancer");
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

    @Test
    @DisplayName("The lock and sen window are cleared at end of turn")
    void locksClearedAtEndOfTurn() {
        gd.playersSilencedThisTurn.add(player2.getId());
        gd.playersCantActivateAbilitiesThisTurn.add(player2.getId());
        gd.senControllerPlayerId = player1.getId();
        gd.senControlledPlayerId = player2.getId();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersSilencedThisTurn).isEmpty();
        assertThat(gd.playersCantActivateAbilitiesThisTurn).isEmpty();
        assertThat(gd.senControllerPlayerId).isNull();
        assertThat(gd.senControlledPlayerId).isNull();
    }

    @Test
    void controllerCanCastSpellFromOpponentsHand() {
        harness.setHand(player1, List.of());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        lockOpponent(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(shock);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllerCanPlayLandFromOpponentsHand() {
        harness.setHand(player1, List.of());
        Forest forest = new Forest();
        harness.setHand(player2, List.of(forest));
        lockOpponent(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(forest);
    }

    @Test
    void newlyDrawnCardsRemainRevealedAfterSourceLeaves() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Shock()));
        lockOpponent(player2);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.getDrawService().resolveDrawCard(gd, player2.getId());
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("Shock"));
    }
}
