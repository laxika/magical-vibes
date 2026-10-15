package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IguanaParrot;
import com.github.laxika.magicalvibes.cards.k.KnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.k.KyoshiBattleFan;
import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({AzulaAlwaysLies.class, IguanaParrot.class, KnowledgeSeeker.class,
        KyoshiBattleFan.class, TurtleDuck.class})
class AzulaAlwaysLiesTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives a creature -1/-1 until end of turn")
    void weakensTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The second mode puts a +1/+1 counter on a creature")
    void putsCounterOnTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modes can target the same creature")
    void bothModesResolveOnSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter mode rejects a player target")
    void counterModeRequiresCreatureTarget() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesAffectOnlyTheirRespectiveTargets() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        Permanent strengthened = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());

        cast(new int[]{0, 1}, List.of(weakened.getId(), strengthened.getId()));

        assertThat(weakened.getPowerModifier()).isEqualTo(-1);
        assertThat(weakened.getToughnessModifier()).isEqualTo(-1);
        assertThat(weakened.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(strengthened.getPowerModifier()).isZero();
        assertThat(strengthened.getToughnessModifier()).isZero();
        assertThat(strengthened.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void bothModesSaveACreatureWithOneToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());

        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        harness.assertOnBattlefield(player1, "Knowledge Seeker");
        harness.assertNotInGraveyard(player1, "Knowledge Seeker");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void firstModeKillsACreatureWithOneToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());

        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Knowledge Seeker");
        harness.assertInGraveyard(player2, "Knowledge Seeker");
    }

    @Test
    void temporaryPenaltyExpiresButCounterRemains() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void counterModeRejectsANoncreaturePermanent() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new KyoshiBattleFan());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstModeRejectsANoncreaturePermanent() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new KyoshiBattleFan());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterStillResolvesWhenTheOtherTargetLeaves() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        Permanent strengthened = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(weakened.getId(), strengthened.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, weakened);

        harness.passBothPriorities();

        assertThat(strengthened.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(strengthened.getPowerModifier()).isZero();
        assertThat(strengthened.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Azula Always Lies");
    }

    @Test
    void penaltyStillResolvesWhenTheCounterTargetLeaves() {
        Permanent weakened = harness.addToBattlefieldAndReturn(player2, new IguanaParrot());
        Permanent strengthened = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(weakened.getId(), strengthened.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, strengthened);

        harness.passBothPriorities();

        assertThat(weakened.getPowerModifier()).isEqualTo(-1);
        assertThat(weakened.getToughnessModifier()).isEqualTo(-1);
        assertThat(weakened.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Azula Always Lies");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new AzulaAlwaysLies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
