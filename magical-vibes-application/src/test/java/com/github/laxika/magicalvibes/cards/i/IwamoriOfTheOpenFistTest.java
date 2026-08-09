package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
