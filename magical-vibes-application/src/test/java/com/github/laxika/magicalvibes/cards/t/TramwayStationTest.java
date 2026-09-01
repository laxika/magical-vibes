package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TramwayStation.class, GrizzlyBears.class})
class TramwayStationTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TramwayStation()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Tramway Station").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one black or red mana")
    void tapAddsChosenMana() {
        Permanent blackStation = addReadyStation(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLACK");

        Permanent redStation = addReadyStation(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(blackStation.isTapped()).isTrue();
        assertThat(redStation.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying two generic, one black, and one red mana sacrifices the land and draws a card")
    void sacrificeDrawsCard() {
        Permanent station = addReadyStation(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(station);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(station.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    private Permanent addReadyStation(Player player) {
        Permanent station = new Permanent(new TramwayStation());
        station.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(station);
        return station;
    }
}
