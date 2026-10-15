package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.cards.r.RedtoothVanguard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerritorialWitchstalker.class, HamletGlutton.class, RedtoothVanguard.class, ThundersteelColossus.class})
class TerritorialWitchstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutLargeCreature() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        harness.addToBattlefield(player2, new RedtoothVanguard());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Gets +1/+0 and can attack when its controller has a creature with power 4 or greater")
    void getsPumpAndCanAttackWithLargeCreature() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        addCreatureReady(player1, new HamletGlutton());
        harness.addToBattlefield(player2, new RedtoothVanguard());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(3);
        declareAttackers(List.of(0));

        assertThat(witchstalker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An opponent's large creature does not satisfy the condition")
    void opponentLargeCreatureDoesNotSatisfyCondition() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        addCreatureReady(player2, new HamletGlutton());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A noncreature Vehicle does not satisfy the condition")
    void uncrewedVehicleDoesNotSatisfyCondition() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        harness.addToBattlefield(player1, new ThundersteelColossus());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The Witchstalker itself can satisfy the condition at exactly four power")
    void selfAtExactlyFourPowerSatisfiesCondition() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        witchstalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player2, new RedtoothVanguard());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, witchstalker)).isEqualTo(5);
        declareAttackers(List.of(0));
        assertThat(witchstalker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The condition is checked again when the ability resolves")
    void conditionLostBeforeResolutionPreventsBothEffects() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        Permanent support = addCreatureReady(player1, new RedtoothVanguard());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Gaining four power after combat begins does not create a trigger")
    void conditionGainedAfterTriggerTimeDoesNotTrigger() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        Permanent support = addCreatureReady(player1, new RedtoothVanguard());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Losing the qualifying creature after resolution does not revoke the effects")
    void conditionLostAfterResolutionDoesNotRevokeEffects() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        Permanent support = addCreatureReady(player1, new RedtoothVanguard());
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new RedtoothVanguard());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        support.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(3);
        declareAttackers(List.of(0));
        assertThat(witchstalker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        addCreatureReady(player1, new HamletGlutton());
        harness.forceActivePlayer(player2);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and attack permission expire at the end of the turn")
    void bothEffectsExpireAtEndOfTurn() {
        Permanent witchstalker = addCreatureReady(player1, new TerritorialWitchstalker());
        addCreatureReady(player1, new HamletGlutton());
        harness.setLibrary(player2, List.of(new TerritorialWitchstalker()));

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(3);
        assertThat(als.canAttack(gd, witchstalker, player1.getId())).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, witchstalker)).isEqualTo(2);
        assertThat(als.canAttack(gd, witchstalker, player1.getId())).isFalse();
    }
}
