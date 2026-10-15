package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimationModule.class, DukharaPeafowl.class, KujarSeedsculptor.class, AetherTheorist.class})
class AnimationModuleTest extends BaseCardTest {

    @Test
    void putsAnotherCounterOfChosenKindOnPermanent() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int moduleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(module);
        harness.activateAbility(player1, moduleIndex, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    void putsAnotherPoisonCounterOnTargetPlayer() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int moduleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(module);
        harness.activateAbility(player1, moduleIndex, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "poison counters");

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void counterPlacementTriggersOptionalServoCreation() {
        harness.addToBattlefield(player1, new AnimationModule());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsAnotherEnergyCounterOnTargetPlayer() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        harness.setHand(player1, List.of(new AetherTheorist()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(module),
                null, player1.getId());
        resolveAllTriggers();

        if (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null) {
            harness.handleListChoice(player1, "energy counters");
        }
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void canDeclineServoPayment() {
        harness.addToBattlefield(player1, new AnimationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Servo")).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canAddCounterToOpponentsPermanentWithoutTriggeringServoCreation() {
        harness.addToBattlefield(player1, new AnimationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "+1/+1 counters");
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    void permanentWithoutCountersIsLegalTargetButGetsNoCounter() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, module.getId());
        resolveAllTriggers();

        assertThat(module.isTapped()).isTrue();
        assertThat(module.getCounters()).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void addsOnlyChosenCounterKindAndNonPlusOneCounterDoesNotTriggerServoCreation() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        module.setCounterCount(CounterType.CHARGE, 2);
        module.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, module.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "charge counters");
        resolveAllTriggers();

        assertThat(module.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(module.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Servo")).isZero();
    }
}
