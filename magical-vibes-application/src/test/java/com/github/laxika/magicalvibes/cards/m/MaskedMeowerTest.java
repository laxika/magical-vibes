package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaskedMeower.class, Forest.class, GrizzlyBears.class})
class MaskedMeowerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and sacrificing Masked Meower draws a card")
    void discardsSacrificesAndDraws() {
        harness.addToBattlefield(player1, new MaskedMeower());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masked Meower");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Masked Meower cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new MaskedMeower());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard and sacrifice are paid before the draw resolves")
    void paysBothCostsBeforeResolution() {
        harness.addToBattlefield(player1, new MaskedMeower());
        harness.setHand(player1, List.of(new MaskedMeower()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Masked Meower");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Masked Meower", "Masked Meower");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land can be discarded to pay Masked Meower's activation cost")
    void canDiscardLand() {
        harness.addToBattlefield(player1, new MaskedMeower());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new MaskedMeower()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Masked Meower");
        harness.assertNotOnBattlefield(player1, "Masked Meower");
        harness.assertInHand(player1, "Masked Meower");
    }

    @Test
    @DisplayName("A tapped Masked Meower can activate without paying mana")
    void canActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new MaskedMeower()).tap();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masked Meower");
        harness.assertInGraveyard(player1, "Masked Meower");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Masked Meower can attack on the turn it enters the battlefield")
    void canAttackImmediately() {
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new MaskedMeower(), "{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
    }
}
