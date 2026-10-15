package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AetherInspector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UntetheredExpress.class, AetherInspector.class})
class UntetheredExpressTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    void crewOneAnimatesExpressAndTapsTheCrew() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());
        Permanent crew = addCreatureReady(player1, new AetherInspector());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(gqs.getEffectivePower(gd, express)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, express)).isEqualTo(4);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackingPutsACounterOnExpress() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());
        addCreatureReady(player1, new AetherInspector());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(express.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, express)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, express)).isEqualTo(5);
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new UntetheredExpress());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewTapsSummoningSickCreatureBeforeAnimationResolves() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new AetherInspector());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(express.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, express)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(express.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotCrewUsingTappedOrOpponentsCreatures() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());
        Permanent crew = addCreatureReady(player1, new AetherInspector());
        crew.tap();
        addCreatureReady(player2, new AetherInspector());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    void animatedExpressCannotCrewItself() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());
        addCreatureReady(player1, new AetherInspector());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(express.isTapped()).isFalse();
    }

    @Test
    void attackCounterPersistsAfterAnimationEndsAndAppliesWhenCrewedAgain() {
        Permanent express = addCreatureReady(player1, new UntetheredExpress());
        Permanent crew = addCreatureReady(player1, new AetherInspector());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isFalse();
        assertThat(express.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        crew.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(gqs.getEffectivePower(gd, express)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, express)).isEqualTo(5);
        assertThat(express.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackCounterIncreasesDamageThatTramplesOverBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new UntetheredExpress());
        addCreatureReady(player1, new AetherInspector());
        Permanent blocker = addCreatureReady(player2, new AetherInspector());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Aether Inspector");
        harness.assertOnBattlefield(player1, "Untethered Express");
    }
}
