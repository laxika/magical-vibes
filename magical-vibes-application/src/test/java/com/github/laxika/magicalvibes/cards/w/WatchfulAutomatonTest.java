package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WatchfulAutomatonTest extends BaseCardTest {

    @Test
    void activatedAbilityScryOneAndKeepsTopCard() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WatchfulAutomaton());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activatedAbilityScryOneCanPutTopCardOnBottom() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WatchfulAutomaton());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), 0, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.get(0)).isNotSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }
}
