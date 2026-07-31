package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class WintersNightTest extends BaseCardTest {

    private Permanent snowForest() {
        Permanent land = new Permanent(new Forest());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        return land;
    }

    @Test
    @DisplayName("Tapping a snow land adds an extra mana of the type it produced")
    void snowLandAddsExtraMana() {
        harness.addToBattlefield(player1, new WintersNight());
        gd.playerBattlefields.get(player1.getId()).add(snowForest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped snow land doesn't untap during its controller's next untap step")
    void snowLandSkipsNextUntap() {
        harness.addToBattlefield(player1, new WintersNight());
        Permanent land = snowForest();
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.tapPermanent(player1, 1);
        assertThat(land.isTapped()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-snow lands are unaffected")
    void nonSnowLandUnaffected() {
        harness.addToBattlefield(player1, new WintersNight());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's snow land also triggers")
    void opponentSnowLandTriggers() {
        harness.addToBattlefield(player1, new WintersNight());
        Permanent land = snowForest();
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(land.getSkipUntapCount()).isEqualTo(1);
    }
}
