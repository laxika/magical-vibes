package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TreefolkSeedlingsTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness equals the number of Forests you control; power stays 2")
    void toughnessEqualsControlledForests() {
        Permanent seedlings = addReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, seedlings)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only your Forests, not opponent Forests")
    void countsOnlyControllersForests() {
        Permanent seedlings = addReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, seedlings)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(1);
    }

    @Test
    @DisplayName("Toughness updates when Forests change")
    void toughnessUpdatesWhenForestsChange() {
        Permanent seedlings = addReady(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectiveToughness(gd, seedlings)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, seedlings)).isEqualTo(2);
    }

    private Permanent addReady(Player player) {
        Permanent permanent = new Permanent(new TreefolkSeedlings());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
