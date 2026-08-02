package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DauntlessRiverMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an Island")
    void getsBoostWithIsland() {
        Permanent marshal = addMarshalReady(player1);
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get +1/+1 without an Island")
    void noBoostWithoutIsland() {
        Permanent marshal = addMarshalReady(player1);

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps target creature for {3}{U}")
    void tapsTargetCreature() {
        addMarshalReady(player1);
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    private Permanent addMarshalReady(Player player) {
        Permanent marshal = new Permanent(new DauntlessRiverMarshal());
        marshal.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(marshal);
        return marshal;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
