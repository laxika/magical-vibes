package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KhodEtlanShiisEnvoy.class, MerfolkOfThePearlTrident.class,
        GrizzlyBears.class, Forest.class})
class KhodEtlanShiisEnvoyTest extends BaseCardTest {

    @Test
    void buffsOtherMatchingCreaturesYouControl() {
        Permanent khod = harness.addToBattlefieldAndReturn(player1, new KhodEtlanShiisEnvoy());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());

        assertThat(gqs.getEffectivePower(gd, khod)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, khod)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(1);
    }

    @Test
    void allLandsAreIslandsAndCanProduceBlueMana() {
        harness.addToBattlefield(player1, new KhodEtlanShiisEnvoy());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.ISLAND)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentForest, CardSubtype.ISLAND)).isTrue();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
