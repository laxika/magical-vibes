package com.github.laxika.magicalvibes.cards.g;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({GravenLore.class, SnowCoveredIsland.class, Forest.class, Mountain.class})
class GravenLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Scry count is based on snow mana spent, then draws three cards")
    void scriesForSnowManaThenDrawsThree() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.tapPermanent(player1, 0);
        castGravenLore(ManaColor.BLUE, 1, 3);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Without snow mana, Graven Lore skips scrying and draws three cards")
    void noSnowManaSkipsScry() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        harness.castFromHand(player1, new GravenLore(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Snow mana paying both colored and generic costs counts, and scry finishes before drawing")
    void allSnowManaCountsAndScryDeterminesDraws() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        Forest third = new Forest();
        Mountain fourth = new Mountain();
        Forest fifth = new Forest();
        Mountain sixth = new Mountain();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SnowCoveredIsland());
            harness.tapPermanent(player1, i);
        }
        harness.setHand(player1, List.of(new GravenLore()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second, third, fourth, fifth);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(4, 1), List.of(3, 0, 2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(fifth, second, sixth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, first, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Graven Lore");
    }

    @Test
    @DisplayName("Scrying more cards than the library contains still draws three when three remain")
    void scryIsLimitedToAvailableCards() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SnowCoveredIsland());
            harness.tapPermanent(player1, i);
        }
        harness.setHand(player1, List.of(new GravenLore()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castGravenLore(ManaColor coloredMana, int coloredAmount, int colorlessAmount) {
        harness.setHand(player1, List.of(new GravenLore()));
        harness.addMana(player1, coloredMana, coloredAmount);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessAmount);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
