package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimationModule.class, DukharaPeafowl.class, KujarSeedsculptor.class, AetherTheorist.class})
class AnimationModuleTest extends BaseCardTest {

    @Test
    @DisplayName("Puts another counter of the chosen kind on the target permanent")
    void putsAnotherCounterOfChosenKindOnPermanent() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int moduleIndex = gd.playerBattlefields.get(player1.getId()).indexOf(module);
        harness.activateAbility(player1, moduleIndex, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "+1/+1 counters");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Gives the target player another poison counter")
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
    @DisplayName("Putting a +1/+1 counter on your permanent offers a Servo for {1}")
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
    @DisplayName("Gives the target player another energy counter")
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
    @DisplayName("The Servo payment can be declined")
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
    @DisplayName("A counter on an opponent's permanent offers no Servo")
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
    @DisplayName("A permanent without counters can be targeted and gets no counter")
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
    @DisplayName("Adds only the chosen kind; a non-+1/+1 counter offers no Servo")
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

    @Test
    @DisplayName("Gives the target player another counter of any kind they have, such as experience or rad counters")
    void putsAnotherExperienceOrRadCounterOnTargetPlayer() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        gd.playerExperienceCounters.put(player2.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(module),
                null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("experience counters", "rad counters");
        harness.handleListChoice(player1, "experience counters");

        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
    }
}
