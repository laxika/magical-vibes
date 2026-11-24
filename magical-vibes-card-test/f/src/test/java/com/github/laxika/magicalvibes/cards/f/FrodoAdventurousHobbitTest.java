package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SamLoyalAttendant;
import com.github.laxika.magicalvibes.model.RingState;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrodoAdventurousHobbit.class, SamLoyalAttendant.class})
class FrodoAdventurousHobbitTest extends BaseCardTest {

    @Test
    void partnerWithLetsTargetPlayerSearchForSam() {
        Card sam = new SamLoyalAttendant();
        harness.setLibrary(player2, List.of(sam));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new FrodoAdventurousHobbit());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPlayerIds()).contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sam);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void attackingAfterSecondRingTemptationDrawsIfFrodoIsRingBearer() {
        Permanent frodo = addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.ringStates.put(player1.getId(), new RingState(1, frodo.getId()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SamLoyalAttendant()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.ringLevel(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTemptTheRingWithoutThreeLifeGained() {
        addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.ringStates).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void partnerSearchCanBeDeclinedByTargetPlayer() {
        Card sam = new SamLoyalAttendant();
        harness.setLibrary(player2, List.of(sam));
        harness.setHand(player2, List.of());
        harness.enterBattlefieldAndReturn(player1, new FrodoAdventurousHobbit());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(sam);
    }

    @Test
    void partnerSearchCanTargetItsController() {
        Card sam = new SamLoyalAttendant();
        harness.setLibrary(player1, List.of(sam));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new FrodoAdventurousHobbit());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sam);
    }

    @Test
    void firstTemptationMakesFrodoRingBearerWithoutDrawing() {
        Permanent frodo = addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of());
        Card sam = new SamLoyalAttendant();
        harness.setLibrary(player1, List.of(sam));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.ringLevel(player1.getId())).isEqualTo(1);
        assertThat(gd.ringBearerId(player1.getId())).isEqualTo(frodo.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sam);
    }

    @Test
    void choosingFrodoAsNewRingBearerDrawsAfterTheChoice() {
        Permanent frodo = addCreatureReady(player1, new FrodoAdventurousHobbit());
        Permanent sam = addCreatureReady(player1, new SamLoyalAttendant());
        gd.ringStates.put(player1.getId(), new RingState(1, sam.getId()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of());
        Card drawnCard = new SamLoyalAttendant();
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(frodo.getId(), sam.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handlePermanentChosen(player1, frodo.getId());
        resolveAllTriggers();

        assertThat(gd.ringLevel(player1.getId())).isEqualTo(2);
        assertThat(gd.ringBearerId(player1.getId())).isEqualTo(frodo.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void choosingAnotherRingBearerPreventsFrodosDraw() {
        Permanent frodo = addCreatureReady(player1, new FrodoAdventurousHobbit());
        Permanent sam = addCreatureReady(player1, new SamLoyalAttendant());
        gd.ringStates.put(player1.getId(), new RingState(1, frodo.getId()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of());
        Card libraryCard = new SamLoyalAttendant();
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handlePermanentChosen(player1, sam.getId());
        resolveAllTriggers();

        assertThat(gd.ringLevel(player1.getId())).isEqualTo(2);
        assertThat(gd.ringBearerId(player1.getId())).isEqualTo(sam.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void excessLifeGainBeforeFrodoEntersStillTemptsOnlyOnceDespiteLifeLoss() {
        gd.lifeGainedThisTurn.put(player1.getId(), 9);
        harness.setLife(player1, 10);
        addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.ringStates.put(player1.getId(), new RingState(1, null));
        harness.setHand(player1, List.of());
        Card drawnCard = new SamLoyalAttendant();
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.ringLevel(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void opponentsLifeGainDoesNotEnableTheAttackTrigger() {
        addCreatureReady(player1, new FrodoAdventurousHobbit());
        gd.lifeGainedThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SamLoyalAttendant()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.ringStates).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
