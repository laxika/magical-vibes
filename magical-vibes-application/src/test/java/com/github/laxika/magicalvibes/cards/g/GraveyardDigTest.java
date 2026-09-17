package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveyardDig.class, GrizzlyBears.class, ShivanDragon.class, Ornithopter.class, Forest.class})
class GraveyardDigTest extends BaseCardTest {

    @Test
    void normalCastOnlyReturnsBlackOrGreenCreatures() {
        Card bears = new GrizzlyBears();
        Card dragon = new ShivanDragon();
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(bears, dragon, ornithopter, new Forest()));
        harness.setHand(player1, List.of(new GraveyardDig()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shivan Dragon");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void alternateCastReturnsCreaturesOfAnyColor() {
        Card bears = new GrizzlyBears();
        Card dragon = new ShivanDragon();
        Card ornithopter = new Ornithopter();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(bears, dragon, ornithopter, forest));
        harness.setHand(player1, List.of(new GraveyardDig()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), dragon.getId(), ornithopter.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId(), ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shivan Dragon");
        harness.assertInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
