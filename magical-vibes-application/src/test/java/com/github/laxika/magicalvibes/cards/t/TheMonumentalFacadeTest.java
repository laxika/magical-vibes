package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent creature = addReadyPermanent(player1, new GrizzlyBears());

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
        Permanent artifact = addReadyPermanent(player1, new TabletOfCompleation());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(facade.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent not controlled by the land's controller")
    void cannotTargetOpponentPermanent() {
        addReadyFacade(player1, 1);
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    @DisplayName("Can only activate the counter ability at sorcery speed")
    void counterAbilityIsSorcerySpeedOnly() {
        addReadyFacade(player1, 1);
        Permanent creature = addReadyPermanent(player1, new GrizzlyBears());
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
        Permanent land = addReadyPermanent(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    private Permanent addReadyFacade(Player player, int oilCounters) {
        Permanent facade = new Permanent(new TheMonumentalFacade());
        facade.setSummoningSick(false);
        facade.setCounterCount(CounterType.OIL, oilCounters);
        gd.playerBattlefields.get(player.getId()).add(facade);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return facade;
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
