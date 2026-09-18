package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(AutomaticLibrarian.class)
class AutomaticLibrarianTest extends BaseCardTest {

    @Test
    @DisplayName("When Automatic Librarian enters, its controller scries 2")
    void scriesTwoOnEnter() {
        Card topCard = new AutomaticLibrarian();
        Card bottomCard = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        castAutomaticLibrarian();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard, bottomCard);
    }

    @Test
    @DisplayName("Scrying 2 can reorder both cards and finish the ETB")
    void resolvesScryTwo() {
        Card topCard = new AutomaticLibrarian();
        Card bottomCard = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        castAutomaticLibrarian();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Automatic Librarian");
    }

    private void castAutomaticLibrarian() {
        harness.setHand(player1, List.of(new AutomaticLibrarian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
