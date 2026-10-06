package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RikkuResourcefulGuardian.class, GrizzlyBears.class, Skinrender.class, AirElemental.class, Solemnity.class})
class RikkuResourcefulGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Moves a counter from an opponent's creature and makes the destination unblockable until end of turn")
    void movesCounterAndMakesDestinationUnblockable() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new GrizzlyBears());
        Permanent destination = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));
        resolveAllTriggers();

        assertThat(rikku.isTapped()).isTrue();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, destination)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, destination)).isFalse();
    }

    @Test
    @DisplayName("Triggers when you put counters on a creature you do not control")
    void triggersForCountersPlacedOnOpponentCreature() {
        addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent target = addCreatureReady(player2, new AirElemental());
        Permanent blocker = addCreatureReady(player1, new AirElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(bls.canBlockAttacker(gd, blocker, target,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Steal can target a creature with no counters without creating a counter or a trigger")
    void noCountersMeansNothingMoves() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), rikku.getId()));
        resolveAllTriggers();

        assertThat(rikku.isTapped()).isTrue();
        assertThat(source.getCounters()).isEmpty();
        assertThat(rikku.getCounters()).isEmpty();
        assertThat(gqs.hasCantBeBlocked(gd, rikku)).isFalse();
    }

    @Test
    @DisplayName("Steal moves exactly one keyword counter and triggers Rikku")
    void movesOneKeywordCounter() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());
        source.setCounterCount(CounterType.VIGILANCE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), rikku.getId()));
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(rikku.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rikku, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, rikku)).isTrue();
    }

    @Test
    @DisplayName("Steal lets its controller choose among different counter types on resolution")
    void controllerChoosesCounterType() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setCounterCount(CounterType.VIGILANCE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), rikku.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(rikku.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("A counter cannot be moved if Solemnity prohibits placing it on the destination")
    void cannotMoveCounterThroughSolemnity() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new Solemnity());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), rikku.getId()));
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rikku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, rikku)).isFalse();
    }

    @Test
    @DisplayName("Rikku's counter-placement trigger does not target and still resolves after shroud is gained")
    void triggerIgnoresShroud() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), rikku.getId()));
        harness.passBothPriorities();
        rikku.getGrantedKeywords().add(Keyword.SHROUD);
        resolveAllTriggers();

        assertThat(rikku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, rikku)).isTrue();
    }

    @Test
    @DisplayName("No counter moves when the source gains hexproof before Steal resolves")
    void illegalSourcePreventsMove() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), rikku.getId()));
        source.getGrantedKeywords().add(Keyword.HEXPROOF);
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rikku.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, rikku)).isFalse();
    }

    @Test
    @DisplayName("Steal cannot be activated during combat")
    void cannotActivateDuringCombat() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent source = addCreatureReady(player2, new RikkuResourcefulGuardian());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(source.getId(), rikku.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rikku.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rikku does not trigger while it has lost all abilities")
    void noTriggerAfterLosingAbilities() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent target = addCreatureReady(player1, new AirElemental());
        rikku.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Steal requires an opponent's creature as source and your creature as destination")
    void rejectsReversedTargetControllers() {
        Permanent rikku = addCreatureReady(player1, new RikkuResourcefulGuardian());
        Permanent opponent = addCreatureReady(player2, new RikkuResourcefulGuardian());
        rikku.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(rikku.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rikku.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

}
