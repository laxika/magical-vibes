package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMonumentalFacade.class, Cankerbloom.class, TabletOfCompleation.class, Forest.class})
class TheMonumentalFacadeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two oil counters")
    void entersWithTwoOilCounters() {
        harness.setHand(player1, List.of(new TheMonumentalFacade()));
        harness.playLand(player1, 0);

        Permanent facade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent facade = addReadyFacade(player1, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(facade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removes an oil counter and puts one on a creature you control")
    void movesOilCounterToControlledCreature() {
        Permanent facade = addReadyFacade(player1, 2);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(facade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can put an oil counter on an artifact you control")
    void putsOilCounterOnControlledArtifact() {
        Permanent facade = addReadyFacade(player1, 1);
        Permanent artifact = addCreatureReady(player1, new TabletOfCompleation());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(facade.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent not controlled by the land's controller")
    void cannotTargetOpponentPermanent() {
        addReadyFacade(player1, 1);
        Permanent creature = addCreatureReady(player2, new Cankerbloom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    @DisplayName("Can only activate the counter ability at sorcery speed")
    void counterAbilityIsSorcerySpeedOnly() {
        addReadyFacade(player1, 1);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadyFacade(player1, 1);
        Permanent land = addCreatureReady(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    void freshlyPlayedLandCanTapForManaWithoutUsingOil() {
        harness.setHand(player1, List.of(new TheMonumentalFacade()));
        harness.playLand(player1, 0);
        Permanent facade = findPermanent(player1, "The Monumental Facade");

        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(facade.isTapped()).isTrue();
    }

    @Test
    void tappedLandCannotActivateCounterAbility() {
        Permanent facade = addReadyFacade(player1, 1);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayOilCostWithAnotherCounterType() {
        Permanent facade = addReadyFacade(player1, 0);
        facade.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(facade.isTapped()).isFalse();
        assertThat(facade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityDoesNotNeedOilAndResolvesImmediately() {
        Permanent facade = addReadyFacade(player1, 0);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(facade.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterAbilityCannotActivateDuringCombat() {
        Permanent facade = addReadyFacade(player1, 1);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(facade.isTapped()).isFalse();
        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void counterAbilityRequiresAnEmptyStack() {
        Permanent facade = addReadyFacade(player1, 1);
        Permanent tablet = addCreatureReady(player1, new TabletOfCompleation());
        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, tablet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(facade.isTapped()).isFalse();
        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void costsArePaidImmediatelyAndAbilitySurvivesSourceLeaving() {
        Permanent facade = addReadyFacade(player1, 2);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());
        creature.setCounterCount(CounterType.OIL, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());

        assertThat(facade.isTapped()).isTrue();
        assertThat(facade.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(facade);
        gd.playerGraveyards.get(player1.getId()).add(facade.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingControlOfTargetDoesNotRefundCostsOrPutCounterOnIt() {
        Permanent facade = addReadyFacade(player1, 1);
        Permanent creature = addCreatureReady(player1, new Cankerbloom());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.OIL)).isZero();
        assertThat(facade.getCounterCount(CounterType.OIL)).isZero();
        assertThat(facade.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyFacade(Player player, int oilCounters) {
        Permanent facade = addCreatureReady(player, new TheMonumentalFacade());
        facade.setCounterCount(CounterType.OIL, oilCounters);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return facade;
    }
}
