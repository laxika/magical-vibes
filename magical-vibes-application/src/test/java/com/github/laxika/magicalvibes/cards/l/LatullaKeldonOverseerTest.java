package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LatullaKeldonOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to a player and discards two cards")
    void dealsXDamageToPlayerAndDiscardsTwoCards() {
        Permanent latulla = addCreatureReady(player1, new LatullaKeldonOverseer());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(latulla.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals X damage to a target creature")
    void dealsXDamageToCreature() {
        addCreatureReady(player1, new LatullaKeldonOverseer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without two cards to discard")
    void cannotActivateWithoutTwoCardsToDiscard() {
        addCreatureReady(player1, new LatullaKeldonOverseer());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land permanent")
    void cannotTargetLandPermanent() {
        addCreatureReady(player1, new LatullaKeldonOverseer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
