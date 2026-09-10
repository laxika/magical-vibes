package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirkwoodPathmaker.class, Forest.class, Mountain.class})
class MirkwoodPathmakerTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands you control")
    void powerAndToughnessEqualControlledLands() {
        Permanent pathmaker = addPathmaker(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, pathmaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pathmaker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only lands controlled by its controller")
    void countsOnlyControllersLands() {
        Permanent pathmaker = addPathmaker(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, pathmaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pathmaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness update when lands change")
    void powerAndToughnessUpdateWhenLandsChange() {
        Permanent pathmaker = addPathmaker(player1);

        assertThat(gqs.getEffectivePower(gd, pathmaker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pathmaker)).isZero();

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        assertThat(gqs.getEffectivePower(gd, pathmaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pathmaker)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gqs.getEffectivePower(gd, pathmaker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, pathmaker)).isZero();
    }

    private Permanent addPathmaker(Player player) {
        Permanent permanent = new Permanent(new MirkwoodPathmaker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
