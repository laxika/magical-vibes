package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DourPortMage.class, GrizzlyBears.class})
class DourPortMageTest extends BaseCardTest {

    @Test
    @DisplayName("Returns another creature you control and draws a card")
    void returnsAnotherCreatureAndDraws() {
        Permanent mage = addCreatureReady(player1, new DourPortMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard());
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not draw when another creature dies")
    void doesNotDrawWhenAnotherCreatureDies() {
        addCreatureReady(player1, new DourPortMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers once when the Mage and another creature leave together")
    void triggersOnceForSimultaneousLeaves() {
        DourPortMage mageCard = new DourPortMage();
        GrizzlyBears bearsCard = new GrizzlyBears();
        Permanent mage = addCreatureReady(player1, mageCard);
        Permanent bears = addCreatureReady(player1, bearsCard);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().beginPermanentLeaveBatch(gd);
            try {
                harness.getPermanentRemovalService().removePermanentToHand(gd, mage);
                harness.getPermanentRemovalService().removePermanentToHand(gd, bears);
            } finally {
                harness.getPermanentRemovalService().endPermanentLeaveBatch(gd);
            }
        });
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(3)
                .contains(mageCard, bearsCard);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new DourPortMage());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }
}
