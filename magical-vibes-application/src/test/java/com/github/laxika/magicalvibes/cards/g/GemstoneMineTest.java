package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed(GemstoneMine.class)
class GemstoneMineTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three mining counters")
    void entersWithThreeMiningCounters() {
        harness.setHand(player1, List.of(new GemstoneMine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Gemstone Mine").getCounterCount(CounterType.MINING))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a mining counter and adds the chosen color of mana")
    void removesCounterAndAddsChosenMana() {
        Permanent mine = addReadyMine(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(mine.getCounterCount(CounterType.MINING)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mine.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices immediately when its ability removes the last mining counter")
    void sacrificesImmediatelyAfterLastCounterIsRemoved() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.MINING, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Gemstone Mine");
        harness.assertInGraveyard(player1, "Gemstone Mine");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not sacrifice when another effect removes the last mining counter")
    void doesNotSacrificeWhenAnotherEffectRemovesLastCounter() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.MINING, 1);
        mine.setCounterCount(CounterType.MINING, 0);

        harness.runStateBasedActions();

        assertThat(mine.getCounterCount(CounterType.MINING)).isZero();
        harness.assertOnBattlefield(player1, "Gemstone Mine");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMine(Player player) {
        Permanent mine = harness.addToBattlefieldAndReturn(player, new GemstoneMine());
        mine.setSummoningSick(false);
        mine.setCounterCount(CounterType.MINING, 3);
        return mine;
    }
}
