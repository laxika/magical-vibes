package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BasilicaShepherd;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopperLonglegs.class, BasilicaShepherd.class})
class CopperLonglegsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Copper Longlegs proliferates")
    void proliferates() {
        addReadyCopperLonglegs(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        harness.assertInGraveyard(player1, "Copper Longlegs");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void sacrificesImmediatelyAndCanActivateWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        source.setSummoningSick(true);
        source.tap();
        Permanent chosen = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Copper Longlegs");
        harness.assertInGraveyard(player1, "Copper Longlegs");
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferatesEveryCounterKindOnChosenPermanentsAndPlayersOnly() {
        addReadyCopperLonglegs(player1);
        Permanent chosen = addCreatureWithCounter(player2);
        chosen.setCounterCount(CounterType.OIL, 2);
        Permanent unchosen = addCreatureWithCounter(player1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chosen.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayChooseNoPermanentsOrPlayers() {
        addReadyCopperLonglegs(player1);
        Permanent unchosen = addCreatureWithCounter(player2);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithNoCountersEvenWhenSacrificedSourceHadCounters() {
        Permanent source = addReadyCopperLonglegs(player1);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        addReadyCopperLonglegs(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Copper Longlegs");
        harness.assertNotInGraveyard(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneGreenMana() {
        addReadyCopperLonglegs(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Copper Longlegs");
        harness.assertNotInGraveyard(player1, "Copper Longlegs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reachAllowsBlockingAFlyingCreature() {
        addCreatureReady(player1, new BasilicaShepherd());
        Permanent blocker = addCreatureReady(player2, new CopperLonglegs());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyCopperLonglegs(Player player) {
        return addCreatureReady(player, new CopperLonglegs());
    }

    private Permanent addCreatureWithCounter(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CopperLonglegs());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return permanent;
    }
}
