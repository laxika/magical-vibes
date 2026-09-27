package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommandBeacon.class, GrizzlyBears.class, EdgarMarkov.class})
class CommandBeaconTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new CommandBeacon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void sacrificesItselfAndPutsACommanderIntoHand() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card commander = new EdgarMarkov();
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Command Beacon");
        harness.assertInGraveyard(player1, "Command Beacon");
        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerCommandZones.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    void choosesOneCommanderWhenMultipleAreInTheCommandZone() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card firstCommander = new EdgarMarkov();
        Card secondCommander = new EdgarMarkov();
        gd.playerCommandZones.get(player1.getId()).addAll(List.of(firstCommander, secondCommander));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CommandZoneCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(secondCommander.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(secondCommander).doesNotContain(firstCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(firstCommander);
    }
}
