package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraprootKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Toughness equals the number of Forests on the battlefield")
    void toughnessEqualsForestsOnBattlefield() {
        Permanent kami = addTraprootKami(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, kami)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Toughness updates as Forests enter and leave the battlefield")
    void toughnessUpdatesWhenForestsChange() {
        Permanent kami = addTraprootKami(player1);

        assertThat(gqs.getEffectiveToughness(gd, kami)).isZero();

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectiveToughness(gd, kami)).isZero();
    }

    private Permanent addTraprootKami(Player player) {
        Permanent permanent = new Permanent(new TraprootKami());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
