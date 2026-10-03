package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.t.TeferisProtege;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Discontinuity.class, AlpineWatchdog.class, TeferisProtege.class})
class DiscontinuityTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {1}{U} during your turn and ends the turn")
    void reducedCostEndsTurn() {
        Discontinuity discontinuity = new Discontinuity();
        harness.castFromHand(player1, discontinuity, "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("The turn ends.")).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discontinuity);
        harness.assertNotInGraveyard(player1, "Discontinuity");
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Costs its full amount outside your turn")
    void reductionOnlyAppliesDuringYourTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Discontinuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduced cost still requires one blue mana")
    void reducedCostRequiresBlueMana() {
        harness.setHand(player1, List.of(new Discontinuity()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Full-cost Discontinuity exiles an opponent's spell and itself")
    void fullCostExilesOpponentSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        AlpineWatchdog watchdog = new AlpineWatchdog();
        harness.castFromHand(player2, watchdog, "{1}{W}");
        Discontinuity discontinuity = new Discontinuity();
        harness.castFromHand(player1, discontinuity, "{3}{U}{U}{U}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(watchdog);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discontinuity);
        harness.assertNotOnBattlefield(player2, "Alpine Watchdog");
        harness.assertNotInGraveyard(player2, "Alpine Watchdog");
        harness.assertNotInGraveyard(player1, "Discontinuity");
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Reduced-cost Discontinuity can exile its controller's spell")
    void reducedCostExilesOwnSpell() {
        AlpineWatchdog watchdog = new AlpineWatchdog();
        harness.castFromHand(player1, watchdog, "{1}{W}");
        Discontinuity discontinuity = new Discontinuity();
        harness.castFromHand(player1, discontinuity, "{1}{U}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(watchdog, discontinuity);
        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Ending combat removes attackers from combat and clears damage")
    void endsCombatAndRemovesDamage() {
        Permanent watchdog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        watchdog.setAttacking(true);
        watchdog.setMarkedDamage(1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castFromHand(player1, new Discontinuity(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(watchdog.isAttacking()).isFalse();
        assertThat(watchdog.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Ending the turn removes an activated ability without resolving it")
    void removesActivatedAbilityWithoutResolving() {
        Permanent protege = harness.addToBattlefieldAndReturn(player1, new TeferisProtege());
        protege.setSummoningSick(false);
        AlpineWatchdog libraryCard = new AlpineWatchdog();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.castFromHand(player1, new Discontinuity(), "{1}{U}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Teferi's Protege"));
        harness.assertOnBattlefield(player1, "Teferi's Protege");
    }

    @Test
    @DisplayName("Cleanup discards before removing damage and ending temporary effects")
    void discardPrecedesCleanupResets() {
        Permanent watchdog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        watchdog.setMarkedDamage(1);
        watchdog.setPowerModifier(3);
        harness.setHand(player1, List.of(
                new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog(),
                new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog()));
        harness.castFromHand(player2, new Discontinuity(), "{3}{U}{U}{U}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
        assertThat(watchdog.getMarkedDamage()).isEqualTo(1);
        assertThat(watchdog.getPowerModifier()).isEqualTo(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertInGraveyard(player1, "Alpine Watchdog");
        assertThat(watchdog.getMarkedDamage()).isZero();
        assertThat(watchdog.getPowerModifier()).isZero();
    }
}
