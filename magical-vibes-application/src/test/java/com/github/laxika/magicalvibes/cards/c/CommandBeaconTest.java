package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandBeacon.class, GrizzlyBears.class})
class CommandBeaconTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Command Beacon adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent beacon = harness.addToBattlefieldAndReturn(player1, new CommandBeacon());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(beacon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Command Beacon puts the command-zone card into its controller's hand")
    void sacrificingReturnsCommanderToHand() {
        GrizzlyBears commander = new GrizzlyBears();
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Permanent beacon = harness.addToBattlefieldAndReturn(player1, new CommandBeacon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerCommandZones.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(beacon);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(beacon.getCard());
    }

    @Test
    @DisplayName("With multiple command-zone cards, Command Beacon prompts for one")
    void choosesOneCommanderWhenThereAreMultiple() {
        GrizzlyBears firstCommander = new GrizzlyBears();
        GrizzlyBears secondCommander = new GrizzlyBears();
        gd.playerCommandZones.get(player1.getId()).addAll(List.of(firstCommander, secondCommander));
        Permanent beacon = harness.addToBattlefieldAndReturn(player1, new CommandBeacon());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(secondCommander.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(secondCommander)
                .doesNotContain(firstCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(firstCommander)
                .doesNotContain(secondCommander);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(beacon);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(beacon.getCard());
    }
}
