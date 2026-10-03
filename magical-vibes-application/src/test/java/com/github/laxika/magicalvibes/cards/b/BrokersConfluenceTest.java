package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokersConfluence.class, GrizzlyBears.class, RodOfRuin.class, SoulWarden.class})
class BrokersConfluenceTest extends BaseCardTest {

    @Test
    void proliferatesThreeTimesWhenThatModeIsRepeated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(new int[]{0, 0, 0}, List.of());
        harness.passBothPriorities();
        proliferate(creature);
        proliferate(creature);
        proliferate(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void phasesOutTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1, 1, 1}, List.of(creature.getId(), creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    void countersTargetActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        cast(new int[]{2, 2, 2}, List.of(rod.getId(), rod.getId(), rod.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetedModesRejectIllegalTargets() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        assertThatThrownBy(() -> cast(new int[]{1, 0, 0}, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedProliferationFinishesAfterExactlyThreeChoices() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(new int[]{0, 0, 0}, List.of());
        harness.passBothPriorities();
        proliferate(creature);
        proliferate(creature);
        proliferate(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Brokers Confluence");
    }

    @Test
    void energyOnlyPlayerCanBeChosenForEveryProliferation() {
        gd.setPlayerEnergyCounters(player1.getId(), 1);

        cast(new int[]{0, 0, 0}, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void proliferatesBeforePhasingEvenWhenModesAreSelectedInReverseOrder() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(new int[]{1, 1, 0}, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        proliferate(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    void countersTriggeredAbilityWhileResolvingAllThreeDifferentModes() {
        harness.addToBattlefield(player2, new SoulWarden());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        java.util.UUID abilityId = gd.stack.getFirst().getTargetableId();

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), abilityId));
        harness.passBothPriorities();
        proliferate(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Soul Warden");
    }

    @Test
    void counterAbilityModeRejectsCreatureSpell() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        java.util.UUID spellId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player2);

        assertThatThrownBy(() -> cast(new int[]{0, 0, 2}, List.of(spellId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void phasesOutThreeDifferentCreaturesAndReturnsThemOnlyOnTheirControllersUntap() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(new int[]{1, 1, 1}, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(first, second, third);
        harness.performUntapStep(player1);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(first, second, third);
        harness.performUntapStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second, third);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void proliferatesEveryCounterKindOnChosenPermanentsAndPlayers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.CHARGE, 2);
        gd.setPlayerEnergyCounters(player1.getId(), 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        cast(new int[]{0, 1, 1}, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), player1.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BrokersConfluence()));
        addMana(player1);
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targetIds, List.of());
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    private void proliferate(Permanent permanent) {
        harness.handleMultiplePermanentsChosen(player1, List.of(permanent.getId()));
    }
}
