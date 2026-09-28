package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UkkimaStalkingShadow.class, GrizzlyBears.class})
class UkkimaStalkingShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Cazur")
    void partnerWithSearchesForCazur() {
        Card cazur = new Card();
        cazur.setName("Cazur, Ruthless Stalker");
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(cazur));

        harness.enterBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cazur);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ukkima cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent ukkima = addCreatureReady(player1, new UkkimaStalkingShadow());
        ukkima.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("When Ukkima leaves, it deals damage and its controller gains life equal to its power")
    void leavesBattlefieldUsesLastKnownPowerForDamageAndLifeGain() {
        Permanent ukkima = harness.addToBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        ukkima.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ukkima));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Ukkima also deals damage and gains life when exiled")
    void exileTriggersLastKnownPowerDamageAndLifeGain() {
        Permanent ukkima = harness.addToBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        ukkima.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, ukkima));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
    }
}
