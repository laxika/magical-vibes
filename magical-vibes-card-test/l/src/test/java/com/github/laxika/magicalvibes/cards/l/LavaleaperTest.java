package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lavaleaper.class, Forest.class, GrizzlyBears.class, ZhalfirinVoid.class})
class LavaleaperTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures, including Lavaleaper, have haste")
    void allCreaturesHaveHaste() {
        Permanent lavaleaper = harness.addToBattlefieldAndReturn(player1, new Lavaleaper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, lavaleaper, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Tapping a basic land adds one additional mana of the type it produced")
    void addsExtraManaForBasicLand() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("The basic-land trigger is symmetric")
    void addsExtraManaForOpponentsBasicLand() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a nonbasic land does not trigger the extra mana")
    void doesNotAddExtraManaForNonbasicLand() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player1, new ZhalfirinVoid());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Lavaleaper adds one mana immediately, without using the stack")
    void multipleLavaleapersAddManaImmediately() {
        harness.addToBattlefield(player1, new Lavaleaper());
        harness.addToBattlefield(player2, new Lavaleaper());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 1);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("Creatures lose the granted haste when Lavaleaper leaves the battlefield")
    void hasteEndsWhenLavaleaperLeaves() {
        Permanent lavaleaper = harness.addToBattlefieldAndReturn(player1, new Lavaleaper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(lavaleaper);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }
}
