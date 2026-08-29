package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KujarSeedsculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnimationModuleTest extends BaseCardTest {

    @Test
    void putsAnotherCounterOfChosenKindOnPermanent() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent module = harness.addToBattlefieldAndReturn(player1, new AnimationModule());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KujarSeedsculptor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Servo"));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
