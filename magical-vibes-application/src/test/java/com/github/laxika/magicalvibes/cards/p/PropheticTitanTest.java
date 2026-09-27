package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PropheticTitan.class, Forest.class, GrizzlyBears.class, Island.class})
class PropheticTitanTest extends BaseCardTest {

    private static final String DAMAGE_MODE = "This creature deals 4 damage to any target.";
    private static final String LIBRARY_MODE =
            "Look at the top four cards of your library. Put one of them into your hand and the rest on the bottom of your library in a random order.";

    @Test
    void withoutDeliriumChoosesOnlyOneMode() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Island(), new GrizzlyBears()));
        castTitan();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(DAMAGE_MODE, LIBRARY_MODE);
        assertThat(choice.options()).doesNotContain("Done");
        assertThat(choice.prompt()).contains("Choose one.");

        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void withDeliriumChoosesBothModes() {
        harness.setGraveyard(player1, List.of(
                typedCard(CardType.CREATURE), typedCard(CardType.LAND),
                typedCard(CardType.ARTIFACT), typedCard(CardType.ENCHANTMENT)));
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Island(), new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTitan();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(DAMAGE_MODE, LIBRARY_MODE);

        harness.handleListChoice(player1, DAMAGE_MODE);
        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice.options()).containsExactly(LIBRARY_MODE, "Done");
        harness.handleListChoice(player1, LIBRARY_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    private void castTitan() {
        harness.setHand(player1, List.of(new PropheticTitan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Card typedCard(CardType type) {
        Card card = new Card();
        card.setType(type);
        return card;
    }
}
