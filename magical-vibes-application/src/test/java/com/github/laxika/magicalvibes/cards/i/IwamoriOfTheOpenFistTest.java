package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IwamoriOfTheOpenFist.class, Forest.class, GrizzlyBears.class, GuanYuSaintedWarrior.class,
        GodsEyeGateToTheReikai.class})
class IwamoriOfTheOpenFistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only legendary creature cards from the opponent's hand")
    void offersOnlyLegendaryCreatures() {
        Card legendaryCreature = new GuanYuSaintedWarrior();
        resolveIwamoriWithOpponentHand(List.of(new Forest(), new GrizzlyBears(), legendaryCreature));

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(2);
    }

    @Test
    @DisplayName("Opponent may put the chosen legendary creature onto the battlefield")
    void opponentPutsChosenLegendaryCreatureOntoBattlefield() {
        Card legendaryCreature = new GuanYuSaintedWarrior();
        resolveIwamoriWithOpponentHand(List.of(new Forest(), new GrizzlyBears(), legendaryCreature));

        harness.handleCardChosen(player2, 2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == legendaryCreature);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB does not offer a legendary noncreature card")
    void doesNotOfferLegendaryNoncreature() {
        Card legendaryLand = new GodsEyeGateToTheReikai();
        Card legendaryCreature = new GuanYuSaintedWarrior();
        resolveIwamoriWithOpponentHand(List.of(legendaryLand, legendaryCreature));

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Opponent may decline putting a legendary creature onto the battlefield")
    void opponentMayDeclineLegendaryCreature() {
        Card legendaryCreature = new GuanYuSaintedWarrior();
        resolveIwamoriWithOpponentHand(List.of(legendaryCreature));

        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(legendaryCreature);
        harness.assertNotOnBattlefield(player2, "Guan Yu, Sainted Warrior");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB does not offer a nonlegendary creature card")
    void doesNotOfferNonlegendaryCreature() {
        resolveIwamoriWithOpponentHand(List.of(new Forest(), new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
    }

    private void resolveIwamoriWithOpponentHand(List<Card> opponentHand) {
        harness.setHand(player1, List.of(new IwamoriOfTheOpenFist()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, opponentHand);
        harness.passBothPriorities();
    }
}
