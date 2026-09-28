package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntellectDevourer.class, GrizzlyBears.class, Forest.class, Unsummon.class})
class IntellectDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent chooses a card from hand to exile with Intellect Devourer")
    void exilesACardFromEachOpponentsHand() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent devourer = castAndResolveTrigger(bears);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(devourer.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("The controller may cast a card exiled with Intellect Devourer using any color of mana")
    void controllerMayCastExiledCardWithAnyColorMana() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent devourer = castAndResolveTrigger(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, gd.getCardsExiledByPermanent(devourer.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller may play a land exiled with Intellect Devourer")
    void controllerMayPlayExiledLand() {
        Forest forest = new Forest();
        Permanent devourer = castAndResolveTrigger(forest);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, gd.getCardsExiledByPermanent(devourer.getId()).getFirst().getId());

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The exiled card returns to its owner's hand when Intellect Devourer leaves")
    void exiledCardReturnsWhenSourceLeaves() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent devourer = castAndResolveTrigger(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, devourer.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(devourer.getId())).isEmpty();
    }

    private Permanent castAndResolveTrigger(Card card) {
        harness.setHand(player1, List.of(new IntellectDevourer()));
        harness.setHand(player2, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);
        return findPermanent(player1, "Intellect Devourer");
    }
}
