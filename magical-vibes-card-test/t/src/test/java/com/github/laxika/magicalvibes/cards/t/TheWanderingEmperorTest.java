package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWanderingEmperor.class, BearerOfMemory.class})
class TheWanderingEmperorTest extends BaseCardTest {

    @Test
    void plusOnePutsCounterAndGrantsFirstStrikeToTargetCreature() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void plusOneMayChooseNoTarget() {
        Permanent emperor = addReadyEmperor(player1, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void minusOneCreatesVigilantSamuraiToken() {
        Permanent emperor = addReadyEmperor(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        Permanent samurai = findPermanent(player1, "Samurai");
        assertThat(samurai.getCard().getPower()).isEqualTo(2);
        assertThat(samurai.getCard().getToughness()).isEqualTo(2);
        assertThat(samurai.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(samurai.getCard().getSubtypes()).contains(CardSubtype.SAMURAI);
        assertThat(samurai.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void minusTwoExilesTappedCreatureAndGainsLife() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player2, new BearerOfMemory());
        creature.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(creature.getCard().getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void loyaltyAbilitiesCanBeActivatedOnOpponentsTurnWhileEmperorEnteredThisTurn() {
        Permanent emperor = harness.enterBattlefieldAndReturn(player1, new TheWanderingEmperor());
        emperor.setCounterCount(CounterType.LOYALTY, 3);
        Permanent creature = addCreatureReady(player2, new BearerOfMemory());
        creature.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(creature.getCard().getId());
    }

    @Test
    void loyaltyAbilitiesCannotBeActivatedOnOpponentsTurnAfterEntryTurn() {
        addReadyEmperor(player1, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    void plusOneWithNoTargetDoesNotAffectExistingCreature() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void plusOneCannotTargetTwoCreatures() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent first = addCreatureReady(player1, new BearerOfMemory());
        Permanent second = addCreatureReady(player2, new BearerOfMemory());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneCannotTargetNoncreaturePlaneswalker() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent opponentEmperor = addReadyEmperor(player2, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentEmperor.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashAllowsCastingAndActivatingDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new TheWanderingEmperor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent emperor = findPermanent(player1, "The Wandering Emperor");
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(countPermanents(player1, "Samurai")).isEqualTo(1);
    }

    @Test
    void firstStrikeExpiresButCounterRemainsAfterCleanup() {
        addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player2, new BearerOfMemory());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void minusTwoCannotTargetUntappedCreature() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player2, new BearerOfMemory());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusTwoDoesNotGainLifeIfTargetUntapsBeforeResolution() {
        Permanent emperor = addReadyEmperor(player1, 3);
        Permanent creature = addCreatureReady(player2, new BearerOfMemory());
        creature.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        creature.untap();
        harness.passBothPriorities();

        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void minusTwoCanExileOwnCreatureAndResolvesAfterEmperorDiesToLoyaltyCost() {
        Permanent emperor = addReadyEmperor(player1, 2);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        creature.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(emperor);
        harness.assertInGraveyard(player1, "The Wandering Emperor");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(creature.getCard().getId());
        harness.assertLife(player1, 22);
    }

    @Test
    void entryTurnTimingPermissionDoesNotAllowSecondLoyaltyActivation() {
        Permanent emperor = harness.enterBattlefieldAndReturn(player1, new TheWanderingEmperor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one loyalty ability");
        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void entryTurnLoyaltyAbilityCanRespondToAnotherAbility() {
        Permanent opponentEmperor = addReadyEmperor(player2, 3);
        Permanent emperor = harness.enterBattlefieldAndReturn(player1, new TheWanderingEmperor());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Samurai")).isEqualTo(1);
        assertThat(countPermanents(player2, "Samurai")).isEqualTo(1);
        assertThat(emperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(opponentEmperor.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void entryTurnTimingPermissionExpiresOnNextTurn() {
        harness.enterBattlefieldAndReturn(player1, new TheWanderingEmperor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    private Permanent addReadyEmperor(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TheWanderingEmperor());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
