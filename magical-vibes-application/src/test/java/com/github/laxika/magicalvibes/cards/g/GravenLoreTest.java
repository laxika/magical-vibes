package com.github.laxika.magicalvibes.cards.g;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        castGravenLore(ManaColor.BLUE, 2, 3);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private void castGravenLore(ManaColor coloredMana, int coloredAmount, int colorlessAmount) {
        harness.setHand(player1, List.of(new GravenLore()));
        harness.addMana(player1, coloredMana, coloredAmount);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessAmount);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
