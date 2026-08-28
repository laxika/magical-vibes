package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyxbloomAncient.class, Forest.class, ManaReflection.class})
class NyxbloomAncientTest extends BaseCardTest {

    @Test
    void triplesManaFromYourPermanent() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void triplesManaFromAnOpponentsPermanent() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void multipleNyxbloomAncientsStackMultiplicatively() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(9);
    }

    @Test
    void stacksMultiplicativelyWithManaReflection() {
        harness.addToBattlefield(player1, new NyxbloomAncient());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(6);
    }
}
