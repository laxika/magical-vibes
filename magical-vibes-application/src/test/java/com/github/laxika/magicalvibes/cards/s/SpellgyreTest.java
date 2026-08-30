package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spellgyre.class, GrizzlyBears.class})
class SpellgyreTest extends BaseCardTest {

    @Test
    void counterModeCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Spellgyre()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void surveilsThenDrawsTwoCards() {
        Card milledCard = new GrizzlyBears();
        Card keptCard = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        Card secondDrawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCard, keptCard, drawnCard, secondDrawnCard));
        harness.setHand(player1, List.of(new Spellgyre()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
    }
}
