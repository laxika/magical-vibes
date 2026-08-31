package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedTheCauldron.class, GrayOgre.class, HillGiant.class})
class FeedTheCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a small creature and creates a Food on your turn")
    void destroysCreatureAndCreatesFoodOnYourTurn() {
        harness.addToBattlefield(player2, new GrayOgre());
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Gray Ogre"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gray Ogre");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Destroys a small creature without creating a Food on an opponent's turn")
    void doesNotCreateFoodOnOpponentsTurn() {
        harness.addToBattlefield(player2, new GrayOgre());
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Gray Ogre"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gray Ogre");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void cannotTargetLargeCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Hill Giant")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }
}
