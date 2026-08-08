package com.github.laxika.magicalvibes.cards.g;

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

class GemstoneMineTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three mining counters")
    void entersWithThreeMiningCounters() {
        harness.setHand(player1, List.of(new GemstoneMine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

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
    @DisplayName("Sacrifices after its last mining counter is removed")
    void sacrificesAfterLastCounterIsRemoved() {
        Permanent mine = addReadyMine(player1);
        mine.setCounterCount(CounterType.MINING, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gemstone Mine");
        harness.assertInGraveyard(player1, "Gemstone Mine");
    }

    private Permanent addReadyMine(Player player) {
        Permanent mine = new Permanent(new GemstoneMine());
        mine.setSummoningSick(false);
        mine.setCounterCount(CounterType.MINING, 3);
        gd.playerBattlefields.get(player.getId()).add(mine);
        return mine;
    }
}
