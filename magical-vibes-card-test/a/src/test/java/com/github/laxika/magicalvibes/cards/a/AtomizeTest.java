package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SolRing;
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

@CardUsed({Atomize.class, DarksteelIngot.class, GrizzlyBears.class, Island.class, SolRing.class})
class AtomizeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonland permanent and proliferates")
    void destroysNonlandPermanentAndProliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.handleMultiplePermanentsChosen(player1, List.of(countered.getId()));

        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new Atomize()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void proliferatesEveryCounterKindOnSelectedPermanentsAndPlayers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.setCounterCount(CounterType.CHARGE, 2);
        land.setCounterCount(CounterType.AGE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 2);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId(), player2.getId()));

        harness.assertInGraveyard(player2, "Sol Ring");
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(land.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void mayChooseNothingToProliferate() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.setCounterCount(CounterType.CHARGE, 1);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Sol Ring");
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void doesNotProliferateWhenOnlyTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.setCounterCount(CounterType.CHARGE, 1);

        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.assertInGraveyard(player1, "Atomize");
    }

    @Test
    void playerWithOnlyEnergyCountersCanBeChosenToProliferate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void proliferatesEnergyAndExperienceAlongsidePoisonAndRadCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player1.getId(), 3);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void proliferatesEvenWhenIndestructibleTargetSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        target.setCounterCount(CounterType.CHARGE, 1);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Atomize");
    }

    @Test
    void resolvesWhenNoPermanentsOrPlayersHaveCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Sol Ring");
        harness.assertInGraveyard(player1, "Atomize");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void destroyedTargetWithOnlyCountersDoesNotLeaveAProliferateChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolRing());
        target.setCounterCount(CounterType.CHARGE, 2);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Sol Ring");
        harness.assertInGraveyard(player1, "Atomize");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Atomize()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
