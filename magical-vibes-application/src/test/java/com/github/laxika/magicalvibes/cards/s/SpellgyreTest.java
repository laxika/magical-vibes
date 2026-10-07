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

    @Test
    @CardUsed(Spellgyre.class)
    void keepsBothSurveilledCardsInChosenOrderBeforeDrawing() {
        Card first = new Spellgyre();
        Card second = new Spellgyre();
        Card remaining = new Spellgyre();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setHand(player1, List.of(new Spellgyre()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @CardUsed(Spellgyre.class)
    void putsBothSurveilledCardsInGraveyardThenDrawsNextTwo() {
        Card first = new Spellgyre();
        Card second = new Spellgyre();
        Card third = new Spellgyre();
        Card fourth = new Spellgyre();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new Spellgyre()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, null, List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed(Spellgyre.class)
    void counterModeCountersAnInstantWithoutResolvingItsDrawMode() {
        Spellgyre target = new Spellgyre();
        Card first = new Spellgyre();
        Card second = new Spellgyre();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{1}, null, List.of());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Spellgyre()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, target.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
