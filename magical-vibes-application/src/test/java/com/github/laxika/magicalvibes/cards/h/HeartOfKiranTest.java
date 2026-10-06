package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AjaniUnyielding;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartOfKiran.class, AjaniUnyielding.class, AetherChaser.class, Ornithopter.class})
class HeartOfKiranTest extends BaseCardTest {

    @Test
    void crewsByTappingCreaturesWithTotalPowerAtLeastThree() {
        Permanent heart = addReadyHeart(player1);
        Permanent firstCreature = addCreatureReady(player1, new AetherChaser());
        Permanent secondCreature = addCreatureReady(player1, new AetherChaser());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    void mayRemoveLoyaltyCounterFromAPlaneswalkerInsteadOfPayingCrew() {
        Permanent heart = addReadyHeart(player1);
        Permanent planeswalker = addPlaneswalker(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(planeswalker.isTapped()).isFalse();
    }

    @Test
    void alternatePaymentCannotRemoveALoyaltyCounterFromANonPlaneswalker() {
        addReadyHeart(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.LOYALTY, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent you control has a counter to remove");
    }

    @Test
    void cannotCrewWithInsufficientPower() {
        Permanent heart = addReadyHeart(player1);
        Permanent creature = addCreatureReady(player1, new AetherChaser());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, heart)).isFalse();
    }

    @Test
    void summoningSickCreaturesCanPayCrewCost() {
        Permanent heart = addReadyHeart(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AetherChaser());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AetherChaser());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, heart)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
    }

    @Test
    void cannotUseOpponentsPlaneswalkerForAlternatePayment() {
        addReadyHeart(player1);
        Permanent planeswalker = addPlaneswalker(player2, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent you control has a counter to remove");

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void canRemoveLastLoyaltyCounterAndStillResolveCrew() {
        Permanent heart = addReadyHeart(player1);
        Permanent planeswalker = addPlaneswalker(player1, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(planeswalker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planeswalker.getCard());
    }

    @Test
    void canPayWithLoyaltyRepeatedlyWithoutTappingPlaneswalker() {
        Permanent heart = addReadyHeart(player1);
        Permanent planeswalker = addPlaneswalker(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(planeswalker.isTapped()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void animationExpiresAtEndOfTurnForEitherPayment(int abilityIndex) {
        Permanent heart = addReadyHeart(player1);
        if (abilityIndex == 0) {
            addCreatureReady(player1, new AetherChaser());
            addCreatureReady(player1, new AetherChaser());
        } else {
            addPlaneswalker(player1, 3);
        }

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, heart)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isFalse();
    }

    @Test
    void loyaltyIsPaidBeforeAnimationResolvesDuringOpponentsTurn() {
        Permanent heart = addReadyHeart(player1);
        Permanent planeswalker = addPlaneswalker(player1, 3);
        planeswalker.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, heart)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, heart)).isTrue();
        assertThat(planeswalker.isTapped()).isTrue();
    }

    private Permanent addReadyHeart(Player player) {
        Permanent heart = harness.addToBattlefieldAndReturn(player, new HeartOfKiran());
        heart.setSummoningSick(false);
        return heart;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new AjaniUnyielding());
        planeswalker.setSummoningSick(false);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }
}
