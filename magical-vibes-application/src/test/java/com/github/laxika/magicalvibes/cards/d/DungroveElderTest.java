package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DungroveElderTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals the number of Forests you control")
    void ptEqualsForestCount() {
        Permanent elder = addElder(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Is 0/0 with no Forests")
    void zeroWithoutForests() {
        Permanent elder = addElder(player1);

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only your Forests, not the opponent's")
    void countsOnlyControllersForests() {
        Permanent elder = addElder(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when a Forest leaves the battlefield")
    void ptUpdatesWhenForestsChange() {
        Permanent elder = addElder(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(0);
    }

    private Permanent addElder(Player player) {
        Permanent permanent = new Permanent(new DungroveElder());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
