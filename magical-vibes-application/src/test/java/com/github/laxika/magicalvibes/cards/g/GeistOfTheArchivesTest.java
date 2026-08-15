package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeistOfTheArchivesTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's upkeep triggers scry 1")
    void controllerUpkeepTriggersScry() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's upkeep does not trigger scry")
    void opponentsUpkeepDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new GeistOfTheArchives());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

}
