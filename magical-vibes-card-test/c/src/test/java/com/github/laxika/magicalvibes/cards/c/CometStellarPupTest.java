package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD6EffectHandler;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CometStellarPup.class, GrizzlyBears.class})
class CometStellarPupTest extends BaseCardTest {

    private RollD6EffectHandler rollHandler;
    private DiceRollService originalDice;

    @BeforeEach
    void captureDice() {
        rollHandler = GameTestEngineContext.get().getBean(RollD6EffectHandler.class);
        originalDice = (DiceRollService) ReflectionTestUtils.getField(rollHandler, "diceRollService");
    }

    @AfterEach
    void restoreDice() {
        ReflectionTestUtils.setField(rollHandler, "diceRollService", originalDice);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollHandler, "diceRollService", new DiceRollService() {
            @Override
            public int roll(int sides) {
                return result;
            }
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void squirrelRollAddsLoyaltyAndGrantsHasteOnlyForThisTurn(int roll) {
        setRoll(roll);
        Permanent comet = addComet(5);
        activateAndResolve(null);

        assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, squirrel, Keyword.HASTE)).isTrue();
        });

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(squirrels).allSatisfy(squirrel ->
                assertThat(gqs.hasKeyword(gd, squirrel, Keyword.HASTE)).isFalse());
    }

    @Test
    void rollThreeOnlyOffersOwnCardsWithManaValueAtMostTwo() {
        setRoll(3);
        Permanent comet = addComet(5);
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new CometStellarPup();
        Card opponentsCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(tooExpensive, eligible));
        harness.setGraveyard(player2, List.of(opponentsCard));

        activateAndResolve(null);

        assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).contains(eligible).doesNotContain(tooExpensive);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void rollThreeWithNoEligibleCardStillRemovesLoyalty() {
        setRoll(3);
        Permanent comet = addComet(5);
        Card tooExpensive = new CometStellarPup();
        harness.setGraveyard(player1, List.of(tooExpensive));

        activateAndResolve(null);

        assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    void damageRollCanChooseAnOpponentsHexproofCreature(int roll) {
        setRoll(roll);
        addComet(5);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);

        beginActivation();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(creature.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    void damageAndLoyaltyLossFinishDuringTheOriginalResolution(int roll) {
        setRoll(roll);
        Permanent comet = addComet(1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        beginActivation();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(comet);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(comet.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5})
    void damageRollCanChooseAPlayerAndUsesLoyaltyBeforeRemovingCounters(int roll) {
        setRoll(roll);
        Permanent comet = addComet(5);
        harness.setLife(player2, 20);

        beginActivation();
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPlayerIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rollSixAddsOneLoyaltyAndRepeatedSixesAccumulateExtraActivations() {
        Permanent comet = addComet(5);
        setRoll(6);
        activateAndResolve(null);
        assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        activateAndResolve(null);
        assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);

        setRoll(1);
        activateAndResolve(null);
        activateAndResolve(null);
        activateAndResolve(null);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void beginActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A low roll removes one loyalty and returns a qualifying graveyard card")
    void lowRollReturnsLowManaValueCard() {
        Permanent comet = addComet(1000);
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        Permanent target = addHighToughnessCreature();

        boolean observed = false;
        for (int i = 0; i < 60 && !observed; i++) {
            int loyaltyBefore = comet.getCounterCount(CounterType.LOYALTY);
            harness.setGraveyard(player1, List.of(returned));
            activateAndResolve(target);
            if (gd.playerHands.get(player1.getId()).contains(returned)) {
                observed = true;
                assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
                assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returned);
            }
            comet.setLoyaltyActivationsThisTurn(0);
        }

        assertThat(observed).isTrue();
    }

    @Test
    @DisplayName("A high roll grants two additional loyalty activations")
    void highRollGrantsTwoExtraActivations() {
        Permanent comet = addComet(1000);
        Permanent target = addHighToughnessCreature();

        boolean observed = false;
        for (int i = 0; i < 60 && !observed; i++) {
            int extraBefore = comet.getExtraLoyaltyActivationsThisTurn();
            activateAndResolve(target);
            observed = comet.getExtraLoyaltyActivationsThisTurn() >= extraBefore + 2;
            comet.setLoyaltyActivationsThisTurn(0);
        }

        assertThat(observed).isTrue();
    }

    private Permanent addComet(int loyalty) {
        Permanent comet = harness.addToBattlefieldAndReturn(player1, new CometStellarPup());
        comet.setCounterCount(CounterType.LOYALTY, loyalty);
        comet.setSummoningSick(false);

        return comet;
    }

    private Permanent addHighToughnessCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setBasePowerOverriddenPermanently(true);
        creature.setPermanentBasePowerOverride(10000);
        creature.setBaseToughnessOverriddenPermanently(true);
        creature.setPermanentBaseToughnessOverride(10000);
        return creature;
    }

    private void activateAndResolve(Permanent target) {
        beginActivation();

        PendingInteraction pending = gd.interaction.activeInteraction();
        if (pending instanceof PendingInteraction.GraveyardChoice choice) {
            harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        } else if (pending instanceof PendingInteraction.PermanentChoice choice) {
            assertThat(choice.validPermanentIds()).contains(target.getId());
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }
    }
}
