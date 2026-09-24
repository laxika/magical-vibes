package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OracleOfTheAlpha.class, GrizzlyBears.class})
class OracleOfTheAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB conjures one of each Power Nine card into the library")
    void etbConjuresPowerNine() {
        harness.setHand(player1, List.of(new OracleOfTheAlpha()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(9).allMatch(Card::isTokenCard);
        assertThat(library).extracting(Card::getName).containsExactlyInAnyOrder(
                "Black Lotus", "Mox Pearl", "Mox Sapphire", "Mox Ruby", "Mox Jet", "Mox Emerald",
                "Ancestral Recall", "Time Walk", "Timetwister");
    }

    @Test
    @DisplayName("Attacking starts a scry 1 interaction")
    void attackScriesOne() {
        Card top = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, bottom));
        addCreatureReady(player1, new OracleOfTheAlpha());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }
}
