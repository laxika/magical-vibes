package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RewardTheFaithful.class, GrizzlyBears.class, WurmcoilEngine.class})
class RewardTheFaithfulTest extends BaseCardTest {

    @Test
    void eachTargetedPlayerGainsGreatestManaValueAmongControllerPermanents() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WurmcoilEngine());
        harness.addToBattlefield(player2, new WurmcoilEngine());
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void evaluatesGreatestManaValueAtResolution() {
        harness.setLife(player2, 10);
        harness.addToBattlefield(player1, new WurmcoilEngine());
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player2.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    void canBeCastWithNoTargets() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
