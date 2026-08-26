package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhaltaStampedeTyrant.class, GrizzlyBears.class, LightningBolt.class})
class GhaltaStampedeTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts any number of creature cards from hand onto the battlefield")
    void putsAnyNumberOfCreaturesFromHandOntoBattlefield() {
        Card ghalta = new GhaltaStampedeTyrant();
        Card bearsOne = new GrizzlyBears();
        Card bearsTwo = new GrizzlyBears();
        Card bolt = new LightningBolt();
        harness.setHand(player1, List.of(ghalta, bearsOne, bearsTwo, bolt));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        var choice = (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactlyInAnyOrder(0, 1);
        assertThat(choice.putAnyNumber()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bolt);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof GrizzlyBears)
                .hasSize(2)
                .allMatch(p -> !p.isTapped());
    }

    @Test
    @DisplayName("Declining the choice puts no creature cards from hand onto the battlefield")
    void decliningPutsNoCreaturesFromHandOntoBattlefield() {
        Card ghalta = new GhaltaStampedeTyrant();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(ghalta, bears));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof GrizzlyBears)
                .isEmpty();
    }
}
