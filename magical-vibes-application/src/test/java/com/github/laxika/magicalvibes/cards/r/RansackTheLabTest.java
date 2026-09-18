package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RansackTheLab.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class RansackTheLabTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest into the graveyard")
    void choosesOneOfTopThreeAndGraveyardsTheRest() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new Shock();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.setHand(player1, List.of(new RansackTheLab()));

        harness.castFromHand(player1, new RansackTheLab(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top1, top3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ransack the Lab");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With fewer than three cards, the chosen card goes to hand and the rest to the graveyard")
    void worksWithSmallLibrary() {
        Card chosen = new GrizzlyBears();
        Card rest = new LlanowarElves();
        harness.setLibrary(player1, List.of(chosen, rest));
        harness.setHand(player1, List.of(new RansackTheLab()));

        harness.castFromHand(player1, new RansackTheLab(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty library, Ransack the Lab simply goes to the graveyard")
    void worksWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RansackTheLab()));

        harness.castFromHand(player1, new RansackTheLab(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ransack the Lab");
    }
}
