package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HopefulInitiate.class, GrizzlyBears.class, LeoninScimitar.class, GloriousAnthem.class})
class HopefulInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Training triggers when attacking with a greater-power creature")
    void trainingTriggersWithGreaterPowerAlly() {
        Permanent initiate = addReadyInitiate(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Hopeful Initiate");

        harness.passBothPriorities();

        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Training does not trigger when attacking alone")
    void trainingDoesNotTriggerAlone() {
        addReadyInitiate(player1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Training does not trigger when ally does not have greater power")
    void trainingDoesNotTriggerWithoutGreaterPower() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // 3 power
        addCreatureReady(player1, new GrizzlyBears()); // 2 power

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removes two +1/+1 counters from one creature and destroys target artifact")
    void destroysArtifactRemovingTwoCountersFromSelf() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Can split the two counters across two creatures")
    void canSplitCountersAcrossCreatures() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        // Exactly one counter each → auto-paid from both
        harness.passBothPriorities();

        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot activate without two +1/+1 counters among controlled creatures")
    void cannotActivateWithoutCounters() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void trainingDoesNotTriggerWithEqualPowerAlly() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void trainingTriggersOnlyOnceWithMultipleGreaterPowerAttackers() {
        Permanent initiate = addReadyInitiate(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void trainingStillResolvesAfterGreaterPowerAttackerLeaves() {
        Permanent initiate = addReadyInitiate(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());

        harness.passBothPriorities();

        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void greaterPowerCreatureThatDoesNotAttackDoesNotEnableTraining() {
        addReadyInitiate(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickInitiateCanPayUsingCountersEntirelyFromAnotherCreature() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new HopefulInitiate());
        initiate.setSummoningSick(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addReadyArtifact(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    void cannotPayUsingOpponentsCounters() {
        addReadyInitiate(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    void cannotPayUsingCountersOnNoncreaturePermanent() {
        addReadyInitiate(player1);
        Permanent artifact = addReadyArtifact(player1);
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void destructionStillResolvesAfterInitiateLeaves() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(initiate);
        gd.playerGraveyards.get(player1.getId()).add(initiate.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    private Permanent addReadyInitiate(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new HopefulInitiate());
    }

    private Permanent addReadyArtifact(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
    }

    private Permanent addReadyEnchantment(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GloriousAnthem());
    }
}
