package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaladhrimGuide.class, Forest.class, Island.class})
class GaladhrimGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield offers scry 2")
    void enteringTheBattlefieldOffersScryTwo() {
        Forest first = new Forest();
        Island second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new GaladhrimGuide());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
