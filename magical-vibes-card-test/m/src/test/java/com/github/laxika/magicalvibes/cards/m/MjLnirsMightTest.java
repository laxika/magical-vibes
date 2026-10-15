package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MjLnirsMight.class, Forest.class})
class MjLnirsMightTest extends BaseCardTest {

    @Test
    void dealsDamageAndExilesTopCardWithPlayPermissionUntilNextTurn() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MjLnirsMight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void requiresAPlayerTarget() {
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MjLnirsMight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unplayedCardRemainsExiledAfterPermissionExpires() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new MjLnirsMight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        Card topCard = new MjLnirsMight();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new MjLnirsMight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void canTargetItsControllerAndExilesOnlyTheTopCard() {
        Card topCard = new Forest();
        Card secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MjLnirsMight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void stillDealsDamageWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MjLnirsMight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mjölnir's Might");
    }
}
