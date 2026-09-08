package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorrentOfFire.class, Forest.class, GrizzlyBears.class, WurmcoilEngine.class})
class TorrentOfFireTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToGreatestManaValueAmongYourPermanents() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WurmcoilEngine());
        harness.addToBattlefield(player2, new WurmcoilEngine());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        addMana(player1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void evaluatesGreatestManaValueAtResolutionAndCanTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WurmcoilEngine());
        harness.addToBattlefield(player1, new WurmcoilEngine());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        addMana(player1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 5);
    }
}
