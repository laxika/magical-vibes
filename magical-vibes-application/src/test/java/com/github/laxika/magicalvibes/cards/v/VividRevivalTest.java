package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosCharm;
import com.github.laxika.magicalvibes.cards.g.GolgariCharm;
import com.github.laxika.magicalvibes.cards.i.IzzetCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VividRevivalTest extends BaseCardTest {

    @Test
    void returnsUpToThreeMulticoloredCardsAndExilesItself() {
        Card first = new IzzetCharm();
        Card second = new BorosCharm();
        Card third = new GolgariCharm();
        Card monocolored = new GrizzlyBears();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(first, second, third, monocolored));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(third.getId(), monocolored.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }

    @Test
    void exilesItselfWhenNoMulticoloredCardsAreInTheGraveyard() {
        Card monocolored = new GrizzlyBears();
        Card spell = new VividRevival();
        harness.setGraveyard(player1, List.of(monocolored));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(monocolored.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }
}
