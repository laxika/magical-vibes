package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeldonWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Keldon Warlord is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent warlord = addWarlordReady(player1);

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Keldon Warlord power and toughness equal non-Wall creatures you control")
    void ptEqualsNonWallControlledCreatures() {
        Permanent warlord = addWarlordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(3);
    }

    @Test
    @DisplayName("Keldon Warlord does not count Walls you control")
    void doesNotCountWalls() {
        Permanent warlord = addWarlordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallOfWood());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(2);
    }

    @Test
    @DisplayName("Keldon Warlord counts only your creatures, not opponent creatures")
    void countsOnlyControllersCreatures() {
        Permanent warlord = addWarlordReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    private Permanent addWarlordReady(Player player) {
        KeldonWarlord card = new KeldonWarlord();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
