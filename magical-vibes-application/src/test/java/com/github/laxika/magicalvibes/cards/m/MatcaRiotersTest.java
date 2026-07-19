package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatcaRiotersTest extends BaseCardTest {

    @Test
    @DisplayName("Matca Rioters power and toughness equal the number of basic land types you control")
    void ptEqualsDomain() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(3);
    }

    @Test
    @DisplayName("Matca Rioters counts each basic land type only once")
    void countsEachTypeOnce() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(2);
    }

    @Test
    @DisplayName("Matca Rioters counts only your lands, not opponent lands")
    void countsOnlyControllersLands() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(1);
    }

    @Test
    @DisplayName("Matca Rioters is 0/0 with no basic lands")
    void zeroWithNoBasicLands() {
        Permanent rioters = addRiotersReady(player1);

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(0);
    }

    @Test
    @DisplayName("Matca Rioters P/T updates when lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent rioters = addRiotersReady(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(1);

        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, rioters)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rioters)).isEqualTo(2);
    }

    private Permanent addRiotersReady(Player player) {
        MatcaRioters card = new MatcaRioters();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
