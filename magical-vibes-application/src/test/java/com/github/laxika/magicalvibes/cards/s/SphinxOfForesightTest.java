package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SphinxOfForesightTest extends BaseCardTest {

    @Test
    void openingHandRevealScryThree() {
        GameTestHarness h = newHarness();
        h.setHand(h.getPlayer1(), java.util.List.of(new SphinxOfForesight()));
        h.skipMulligan();

        h.passBothPriorities();
        h.handleMayAbilityChosen(h.getPlayer1(), true);

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNotNull()
                .extracting(PendingInteraction.Scry::cards)
                .asList()
                .hasSize(3);
    }

    @Test
    void decliningOpeningHandRevealDoesNotScry() {
        GameTestHarness h = newHarness();
        h.setHand(h.getPlayer1(), java.util.List.of(new SphinxOfForesight()));
        h.skipMulligan();

        h.passBothPriorities();
        h.handleMayAbilityChosen(h.getPlayer1(), false);

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void battlefieldSphinxScriesOneAtUpkeep() {
        GameTestHarness h = newHarness();
        h.addToBattlefield(h.getPlayer1(), new SphinxOfForesight());
        h.skipMulligan();

        h.passBothPriorities();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNotNull()
                .extracting(PendingInteraction.Scry::cards)
                .asList()
                .hasSize(1);
    }

    private GameTestHarness newHarness() {
        GameTestHarness h = new GameTestHarness();
        h.getGameData().alwaysOfferPriorityWindows = true;
        return h;
    }
}
