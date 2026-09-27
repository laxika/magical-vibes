package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfHoofbeats.class, VenerableKnight.class, GrizzlyBears.class})
class HeraldOfHoofbeatsTest extends BaseCardTest {

    @Test
    @DisplayName("Herald of Hoofbeats gives other Knights you control horsemanship")
    void givesOtherKnightsHorsemanship() {
        harness.addToBattlefield(player1, new HeraldOfHoofbeats());
        harness.addToBattlefield(player1, new VenerableKnight());

        Permanent knight = findPermanent(player1, "Venerable Knight");

        assertThat(gqs.hasKeyword(gd, knight, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Herald of Hoofbeats does not give horsemanship to non-Knights or opposing Knights")
    void onlyGivesHorsemanshipToOtherKnightsYouControl() {
        harness.addToBattlefield(player1, new HeraldOfHoofbeats());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new VenerableKnight());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opposingKnight = findPermanent(player2, "Venerable Knight");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HORSEMANSHIP)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingKnight, Keyword.HORSEMANSHIP)).isFalse();
    }
}
