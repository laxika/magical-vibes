package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupplyDemand.class, BorosCharm.class, GrizzlyBears.class})
class SupplyDemandTest extends BaseCardTest {

    @Test
    @DisplayName("Supply creates X Saproling tokens")
    void supplyCreatesXTokens() {
        harness.setHand(player1, List.of(new SupplyDemand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, new int[]{0}, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SAPROLING));
    }

    @Test
    @DisplayName("Demand searches for a multicolored card and shuffles")
    void demandSearchesForMulticoloredCard() {
        BorosCharm multicolored = new BorosCharm();
        GrizzlyBears monocolored = new GrizzlyBears();
        harness.setLibrary(player1, List.of(monocolored, multicolored));
        harness.setHand(player1, List.of(new SupplyDemand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(multicolored);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(multicolored);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(monocolored);
    }
}
