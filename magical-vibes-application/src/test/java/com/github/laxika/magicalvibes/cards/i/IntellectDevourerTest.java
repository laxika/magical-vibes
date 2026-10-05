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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, devourer.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(devourer.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is exiled if Intellect Devourer leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears, new Unsummon()));
        harness.castFromHand(player1, new IntellectDevourer(), "{3}{B}");
        harness.passBothPriorities();
        Permanent devourer = findPermanent(player1, "Intellect Devourer");

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 1, devourer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.getCardsExiledByPermanent(devourer.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent with an empty hand has no card to exile")
    void emptyOpponentHand() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new IntellectDevourer(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Intellect Devourer").getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses which single card to exile")
    void opponentChoosesOneCard() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player2, List.of(bears, forest));
        harness.castFromHand(player1, new IntellectDevourer(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Intellect Devourer").getId()))
                .containsExactly(forest);
    }

    @Test
    @DisplayName("Body Thief does not allow casting a creature during combat")
    void creatureStillRequiresSorceryTiming() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent devourer = castAndResolveTrigger(bears);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(devourer.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("A creature cast from exile does not return when Intellect Devourer leaves")
    void playedCardDoesNotReturn() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent devourer = castAndResolveTrigger(bears);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.ensurePriority(player1);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, devourer.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    private Permanent castAndResolveTrigger(Card card) {
        harness.setHand(player2, List.of(card));
        harness.castFromHand(player1, new IntellectDevourer(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);
        return findPermanent(player1, "Intellect Devourer");
    }
}
