package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaplessResearcher.class, GrizzlyBears.class, Shock.class})
class HaplessResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it draws a card, then discards a card")
    void sacrificesDrawsThenDiscards() {
        harness.addToBattlefield(player1, new HaplessResearcher());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Hapless Researcher");
        harness.assertInGraveyard(player1, "Hapless Researcher");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
