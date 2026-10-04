package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Gray Ogre"));

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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Gray Ogre"));

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

    @Test
    @DisplayName("Can destroy your own creature and create Food")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrayOgre());
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Gray Ogre"));

        harness.assertInGraveyard(player1, "Gray Ogre");
        harness.assertOnBattlefield(player1, "Food");
        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Food can be sacrificed immediately for two mana to gain three life")
    void createdFoodCanBeUsedImmediately() {
        harness.addToBattlefield(player2, new GrayOgre());
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 10);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Gray Ogre"));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Creates no Food if the only target disappears before resolution")
    void noFoodWhenTargetDisappears() {
        harness.addToBattlefield(player2, new GrayOgre());
        var targetId = harness.getPermanentId(player2, "Gray Ogre");
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.setHand(player2, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertInGraveyard(player2, "Gray Ogre");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertInGraveyard(player1, "Feed the Cauldron");
    }

    @Test
    @DisplayName("Cannot target a noncreature Food despite its zero mana value")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new GrayOgre());
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Gray Ogre"));
        harness.setHand(player1, List.of(new FeedTheCauldron()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Food")))
                .isInstanceOf(IllegalStateException.class);
    }
}
