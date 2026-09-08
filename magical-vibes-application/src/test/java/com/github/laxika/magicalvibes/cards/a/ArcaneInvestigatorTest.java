package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneInvestigator.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class ArcaneInvestigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Search the Room resolves a d20 result")
    void resolvesD20Result() {
        Card top = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new ArcaneInvestigator());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibraryRevealChoice) {
            harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibraryReorder reorder) {
                gs.handleInteractionAnswer(gd, player1,
                        new InteractionAnswer.CardOrder(List.of(
                                reorder.cards().indexOf(top), reorder.cards().indexOf(third))));
            }

            assertThat(gd.playerHands.get(player1.getId())).contains(second);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, third);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).contains(top);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
