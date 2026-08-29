package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaughingMad.class, GrizzlyBears.class, Island.class})
class LaughingMadTest extends BaseCardTest {

    @Test
    void castsFromHandByDiscardingAndDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new LaughingMad(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Laughing Mad");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void flashbackAlsoRequiresDiscardingAndExilesTheSpell() {
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(new LaughingMad()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashbackWithDiscard(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Laughing Mad"));
    }

    @Test
    void cannotCastWithoutDiscardingAnotherCard() {
        harness.setHand(player1, List.of(new LaughingMad()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
