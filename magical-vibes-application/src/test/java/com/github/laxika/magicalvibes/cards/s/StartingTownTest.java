package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StartingTown.class)
class StartingTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters untapped during the controller's first three turns")
    void entersUntappedDuringFirstThreeTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new StartingTown()));

        harness.playLand(player1, 0);

        Permanent town = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(town.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters tapped after the controller's third turn")
    void entersTappedAfterThirdTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new StartingTown()));

        harness.playLand(player1, 0);

        Permanent town = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(town.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorlessMana() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays 1 life and adds one mana of the chosen color")
    void paysLifeAndAddsChosenColor() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new StartingTown());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
