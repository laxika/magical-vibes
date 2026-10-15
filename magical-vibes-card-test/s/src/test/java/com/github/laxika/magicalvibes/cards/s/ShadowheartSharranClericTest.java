package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowheartSharranCleric.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Shock.class, Swamp.class})
class ShadowheartSharranClericTest extends BaseCardTest {

    @Test
    void endStepTriggerDealsDamageToEachPlayer() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void specializeRequiresAPlayerAtThirteenOrLessLife() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 14);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("13 or less life");
    }

    @Test
    void whiteSpecializationCreatesAKnightWhenYouLoseLifeOnYourTurn() {
        Permanent shadowheart = specialize(new Plains(), 0);
        loseTwoLifeDuringOwnTurn();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
        assertThat(shadowheart.getCard().getName()).isEqualTo("Shadowheart, Cleric of Order");
    }

    @Test
    void blueSpecializationDrawsWhenYouLoseLifeOnYourTurn() {
        specialize(new Island(), 1);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        loseTwoLifeDuringOwnTurn();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void blackSpecializationGainsLifeFromItsEndStepDamage() {
        specialize(new Swamp(), 2);
        harness.setLife(player1, 13);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void redSpecializationDealsTheLostLifeAsDamageToEachOpponent() {
        specialize(new Mountain(), 3);
        loseTwoLifeDuringOwnTurn();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void greenSpecializationAddsCountersAndCannotBeBlockedBySmallCreatures() {
        Permanent shadowheart = specialize(new Forest(), 4);
        loseTwoLifeDuringOwnTurn();

        assertThat(shadowheart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        shadowheart.setAttacking(true);
        shadowheart.setAttackTarget(player2.getId());
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shadowheart);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStep(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void canSpecializeWhenOnlyOpponentHasThirteenLife() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 20);
        harness.setLife(player2, 13);
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        loseTwoLifeDuringOwnTurn();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    void canDiscardAColoredNonlandToSpecialize() {
        specialize(new Shock(), 3);
        harness.assertInGraveyard(player1, "Shock");
        loseTwoLifeDuringOwnTurn();

        harness.assertLife(player2, 18);
    }

    @Test
    void cannotSpecializeWithCardOfWrongColorAndLandType() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 13);
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Island");
    }

    @Test
    void cannotSpecializeDuringOpponentsTurn() {
        addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 13);
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Plains");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3, 4})
    void specializedLifeLossDoesNotTriggerDuringOpponentsTurn(int abilityIndex) {
        Permanent shadowheart = specializeForIndex(abilityIndex);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Knight")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(shadowheart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3, 4})
    void specializedLifeLossTriggersOncePerEventNotPerLifeLost(int abilityIndex) {
        Permanent shadowheart = specializeForIndex(abilityIndex);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        loseTwoLifeDuringOwnTurn();
        loseTwoLifeDuringOwnTurn();

        harness.assertLife(player1, 9);
        switch (abilityIndex) {
            case 0 -> assertThat(findPermanents(player1, "Knight")).hasSize(2);
            case 1 -> assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            case 3 -> harness.assertLife(player2, 16);
            case 4 -> assertThat(shadowheart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            default -> throw new IllegalArgumentException();
        }
    }

    private Permanent specializeForIndex(int abilityIndex) {
        return switch (abilityIndex) {
            case 0 -> specialize(new Plains(), 0);
            case 1 -> specialize(new Island(), 1);
            case 3 -> specialize(new Mountain(), 3);
            case 4 -> specialize(new Forest(), 4);
            default -> throw new IllegalArgumentException();
        };
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3, 4})
    void specializedEndStepDamageAlsoTriggersItsLifeLossAbility(int abilityIndex) {
        Permanent shadowheart = specializeForIndex(abilityIndex);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToEndStep(player1);

        harness.assertLife(player1, 12);
        harness.assertLife(player2, abilityIndex == 3 ? 18 : 19);
        switch (abilityIndex) {
            case 0 -> assertThat(findPermanents(player1, "Knight")).hasSize(1);
            case 1 -> assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            case 3 -> { }
            case 4 -> assertThat(shadowheart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            default -> throw new IllegalArgumentException();
        }
    }

    @Test
    void specializationPersistsAfterDyingAndBeingCastAgain() {
        Permanent shadowheart = specialize(new Mountain(), 3);
        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, shadowheart.getId());
            resolveAllTriggers();
        }
        harness.assertInGraveyard(player1, "Shadowheart, Cleric of War");
        com.github.laxika.magicalvibes.model.Card specialized = gd.playerGraveyards.get(player1.getId())
                .stream().filter(card -> card.getName().equals("Shadowheart, Cleric of War"))
                .findFirst().orElseThrow();
        gd.playerGraveyards.get(player1.getId()).remove(specialized);
        harness.setHand(player1, List.of(specialized));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        loseTwoLifeDuringOwnTurn();

        harness.assertOnBattlefield(player1, "Shadowheart, Cleric of War");
        harness.assertLife(player2, 18);
    }

    private Permanent specialize(com.github.laxika.magicalvibes.model.Card discardedCard,
                                 int abilityIndex) {
        Permanent shadowheart = addCreatureReady(player1, new ShadowheartSharranCleric());
        harness.setLife(player1, 13);
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return shadowheart;
    }

    private void loseTwoLifeDuringOwnTurn() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(activePlayer);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
