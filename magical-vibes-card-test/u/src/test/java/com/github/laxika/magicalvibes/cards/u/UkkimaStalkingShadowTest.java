package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CazurRuthlessStalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UkkimaStalkingShadow.class, CazurRuthlessStalker.class})
class UkkimaStalkingShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Cazur")
    void partnerWithSearchesForCazur() {
        CazurRuthlessStalker cazur = new CazurRuthlessStalker();
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
        addCreatureReady(player2, new CazurRuthlessStalker());
        Permanent ukkima = addCreatureReady(player1, new UkkimaStalkingShadow());
        ukkima.setAttacking(true);

        prepareDeclareBlockers();

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

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
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

    @Test
    @DisplayName("The targeted player may decline the partner search")
    void partnerSearchCanBeDeclined() {
        CazurRuthlessStalker cazur = new CazurRuthlessStalker();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(cazur));

        harness.enterBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(cazur);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Partner with may target Ukkima's controller")
    void partnerSearchCanTargetController() {
        CazurRuthlessStalker cazur = new CazurRuthlessStalker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(cazur));

        harness.enterBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cazur);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The partner search cannot find a differently named card")
    void partnerSearchWithNoCazurFindsNothing() {
        UkkimaStalkingShadow otherCard = new UkkimaStalkingShadow();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(otherCard));

        harness.enterBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Returning Ukkima to hand can target its controller without losing before the life gain")
    void returningToHandTriggersAndCanTargetController() {
        Permanent ukkima = harness.addToBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        ukkima.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player2, new CazurRuthlessStalker());
        harness.setLife(player1, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ukkima));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ukkima, Stalking Shadow");
        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The targeted player may fail to find Cazur even when it is in their library")
    void partnerSearchMayFailToFind() {
        CazurRuthlessStalker cazur = new CazurRuthlessStalker();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(cazur));

        harness.enterBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(cazur);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("Zero or negative last-known power deals no damage and gains no life")
    void nonpositivePowerDealsNoDamageAndGainsNoLife(int counters) {
        Permanent ukkima = harness.addToBattlefieldAndReturn(player1, new UkkimaStalkingShadow());
        ukkima.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, counters);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ukkima, Stalking Shadow");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
