package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KeenBuccaneer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmonkhetRaceway.class, Forest.class, KeenBuccaneer.class})
class AmonkhetRacewayTest extends BaseCardTest {

    @Test
    void startsEnginesAndIncreasesSpeedOnlyOncePerTurn() {
        addRaceway(player1);
        forceSorcerySpeed(player1);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void tapsForColorlessMana() {
        addRaceway(player1);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void maxSpeedAbilityGrantsHasteUntilEndOfTurn() {
        addRaceway(player1);
        Permanent target = addCreatureReady(player1, new KeenBuccaneer());
        gd.playerSpeeds.put(player1.getId(), 4);
        forceSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeedAndCreatureTarget() {
        Permanent raceway = addRaceway(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerSpeeds.put(player1.getId(), 3);
        forceSorcerySpeed(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");

        gd.playerSpeeds.put(player1.getId(), 4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(raceway).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void cannotGrantHasteBeforeSpeedIncreaseResolves() {
        addRaceway(player1);
        Permanent target = addCreatureReady(player1, new KeenBuccaneer());
        gd.playerSpeeds.put(player1.getId(), 3);
        forceSorcerySpeed(player1);

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");

        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void lifeLossOnOpponentsTurnDoesNotIncreaseSpeed() {
        addRaceway(player1);
        harness.runStateBasedActions();
        forceSorcerySpeed(player2);

        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1);
            harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1);
        });

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void existingSpeedIsNotResetByAnotherRaceway() {
        gd.playerSpeeds.put(player1.getId(), 3);
        addRaceway(player1);
        addRaceway(player1);

        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void speedPersistsAfterRacewayLeavesBattlefield() {
        Permanent raceway = addRaceway(player1);
        forceSorcerySpeed(player1);
        harness.runStateBasedActions();
        gd.playerBattlefields.get(player1.getId()).remove(raceway);
        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void ownLifeLossDoesNotIncreaseSpeedAndMaxSpeedCannotIncreaseFurther() {
        addRaceway(player1);
        forceSorcerySpeed(player1);
        harness.runStateBasedActions();

        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player1.getId(), 1));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        gd.playerSpeeds.put(player1.getId(), 4);
        harness.inMutationScope(() ->
                harness.getTriggerCollectionService().checkLifeLossTriggers(gd, player2.getId(), 1));
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canGrantHasteToOpponentsCreatureAtInstantSpeedAndAfterLosingMaxSpeed() {
        Permanent raceway = addRaceway(player1);
        Permanent target = addCreatureReady(player2, new KeenBuccaneer());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(raceway.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    private Permanent addRaceway(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AmonkhetRaceway());
    }

    private void forceSorcerySpeed(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
