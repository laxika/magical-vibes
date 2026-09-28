package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SlumberingWaterways.class)
class SlumberingWaterwaysTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new SlumberingWaterways()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Slumbering Waterways").isTapped()).isTrue();
    }

    @Test
    void tapsForGreenOrBlueMana() {
        Permanent greenWaterway = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());
        Permanent blueWaterway = harness.addToBattlefieldAndReturn(player1, new SlumberingWaterways());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(greenWaterway.isTapped()).isTrue();
        assertThat(blueWaterway.isTapped()).isTrue();
    }
}
