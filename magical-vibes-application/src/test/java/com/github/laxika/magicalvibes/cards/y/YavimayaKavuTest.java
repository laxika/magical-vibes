package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CinderShade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VodalianZombie;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaKavu.class, CinderShade.class, YavimayaBarbarian.class,
        VodalianZombie.class, Mountain.class, Forest.class})
class YavimayaKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Power counts red creatures and toughness counts green creatures on the battlefield")
    void countsMatchingCreaturesOnAllBattlefields() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());
        harness.addToBattlefield(player1, new CinderShade());
        harness.addToBattlefield(player2, new CinderShade());
        harness.addToBattlefield(player2, new YavimayaBarbarian());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count noncreatures or creatures of other colors")
    void ignoresNonmatchingPermanents() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new VodalianZombie());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness update as matching creatures enter")
    void updatesWhenCreatureCountsChange() {
        Permanent kavu = addCreatureReady(player1, new YavimayaKavu());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);

        harness.addToBattlefield(player2, new CinderShade());
        harness.addToBattlefield(player1, new YavimayaBarbarian());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }
}
