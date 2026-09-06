package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WintersNight.class, SnowCoveredForest.class, SchoolOfTheUnseen.class})
class WintersNightTest extends BaseCardTest {

    private Permanent addSnowCoveredForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredForest());
    }

    private Permanent addActivatedSnowLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new SchoolOfTheUnseen());
        TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        return land;
    }

    @Test
    @DisplayName("Tapping a snow land adds an extra mana of the type it produced")
    void snowLandAddsExtraMana() {
        harness.addToBattlefield(player1, new WintersNight());
        addSnowCoveredForest(player1);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void activatedSnowLandAddsExtraMana() {
        harness.addToBattlefield(player1, new WintersNight());
        addActivatedSnowLand(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped snow land doesn't untap during its controller's next untap step")
    void snowLandSkipsNextUntap() {
        harness.addToBattlefield(player1, new WintersNight());
        Permanent land = addSnowCoveredForest(player1);

        harness.tapPermanent(player1, 1);
        assertThat(land.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-snow lands are unaffected")
    void nonSnowLandUnaffected() {
        harness.addToBattlefield(player1, new WintersNight());
        harness.addToBattlefield(player1, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's snow land also triggers")
    void opponentSnowLandTriggers() {
        harness.addToBattlefield(player1, new WintersNight());
        Permanent land = addSnowCoveredForest(player2);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(land.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void activatedAnyColorSnowLandAddsExtraMana() {
        harness.addToBattlefield(player1, new WintersNight());
        addActivatedSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }
}
