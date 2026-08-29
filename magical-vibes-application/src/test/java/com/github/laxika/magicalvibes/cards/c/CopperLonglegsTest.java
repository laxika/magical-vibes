package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyCopperLonglegs(Player player) {
        Permanent permanent = new Permanent(new CopperLonglegs());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureWithCounter(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
