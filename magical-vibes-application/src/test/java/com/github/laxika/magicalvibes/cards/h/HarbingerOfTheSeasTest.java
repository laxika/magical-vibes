package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarbingerOfTheSeas.class, Forest.class, Glimmerpost.class})
class HarbingerOfTheSeasTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic land taps for blue instead of its normal mana")
    void nonbasicLandProducesBlue() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic land is unaffected")
    void basicLandUnaffected() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nonbasic land resumes its normal mana after Harbinger of the Seas leaves")
    void normalManaResumesWhenHarbingerLeaves() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player1, new HarbingerOfTheSeas());
        Permanent harbinger = gd.playerBattlefields.get(player1.getId()).get(1);

        gd.playerBattlefields.get(player1.getId()).remove(harbinger);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }
}
