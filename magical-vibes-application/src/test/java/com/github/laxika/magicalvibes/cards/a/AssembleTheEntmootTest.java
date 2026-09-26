package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssembleTheEntmoot.class, Forest.class, GrizzlyBears.class, ZuranOrb.class})
class AssembleTheEntmootTest extends BaseCardTest {

    @Test
    void givesReachToCreaturesYouControl() {
        harness.addToBattlefield(player1, new AssembleTheEntmoot());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.REACH)).isFalse();
    }

    @Test
    void sacrificesToCreateTappedTreefolkSizedByLifeGainedWithReachCounters() {
        harness.addToBattlefield(player1, new AssembleTheEntmoot());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Assemble the Entmoot");
        List<Permanent> treefolk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treefolk"))
                .toList();
        assertThat(treefolk).hasSize(3);
        assertThat(treefolk).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
            assertThat(token.getCounterCount(CounterType.REACH)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.REACH)).isTrue();
        });
    }
}
