package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MogMoogleWarrior.class, SwordsToPlowshares.class})
class MogMoogleWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may discard, draws for discarding, and Mog applies both discard riders")
    void discardRidersApplyToAllDiscardedTypes() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new MogMoogleWarrior()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player1, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player2, List.of(new MogMoogleWarrior()));

        resolveEndStepTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Swords to Plowshares");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mog, Moogle Warrior");
        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        List<Permanent> moogles = findPermanents(player1, "Moogle");
        assertThat(moogles).hasSize(1);
        assertThat(moogles.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining leaves hands and Mog unchanged")
    void decliningDoesNothing() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new MogMoogleWarrior()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player1, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player2, List.of(new MogMoogleWarrior()));

        resolveEndStepTrigger();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mog, Moogle Warrior");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Swords to Plowshares", "Mog, Moogle Warrior");
        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Moogle")).isEmpty();
    }

    @Test
    void discardChoicesRemainHiddenUntilEveryoneHasChosen() {
        harness.addToBattlefield(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new MogMoogleWarrior()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player1, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player2, List.of(new MogMoogleWarrior()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player1, "Mog, Moogle Warrior");
        harness.assertInGraveyard(player2, "Swords to Plowshares");
    }

    @Test
    void twoCreatureDiscardsCreateOnlyOneTokenAndNoCounters() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new MogMoogleWarrior()));
        harness.setHand(player2, List.of(new MogMoogleWarrior()));
        harness.setLibrary(player1, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player2, List.of(new SwordsToPlowshares()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanents(player1, "Moogle")).hasSize(1);
        assertThat(findPermanents(player1, "Moogle").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Moogle")).isEmpty();
    }

    @Test
    void opponentNoncreatureDiscardAddsCounterEvenWhenControllerHasEmptyHand() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player1, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player2, List.of(new MogMoogleWarrior()));

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            resolveEndStepTrigger();
            harness.handleMayAbilityChosen(player2, true);
            harness.handleCardChosen(player2, 0);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        });

        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Moogle")).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new MogMoogleWarrior()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void twoNoncreatureDiscardsAddOnlyOneCounterToControllersMoogles() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        Permanent opposingMog = harness.addToBattlefieldAndReturn(player2, new MogMoogleWarrior());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player1, List.of(new MogMoogleWarrior()));
        harness.setLibrary(player2, List.of(new MogMoogleWarrior()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingMog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Moogle")).isEmpty();
    }

    @Test
    void emptyHandsProduceNoDrawsTokensOrCounters() {
        Permanent mog = harness.addToBattlefieldAndReturn(player1, new MogMoogleWarrior());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player2, List.of(new MogMoogleWarrior()));

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            resolveEndStepTrigger();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        });

        assertThat(mog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Moogle")).isEmpty();
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
