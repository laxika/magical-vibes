package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DavrosDalekCreator.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class DavrosDalekCreatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Dalek and lets a qualifying opponent choose to draw")
    void createsDalekAndOpponentChoosesDraw() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        var drawn = new GrizzlyBears();
        var opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(opponentCard));
        dealThreeDamageToOpponent();

        resolveEndStep();

        harness.assertOnBattlefield(player1, "Dalek");
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                ChoiceContext.VillainousChoice.DRAW,
                ChoiceContext.VillainousChoice.DISCARD);

        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DRAW);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Lets a qualifying opponent choose to discard")
    void opponentChoosesDiscard() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        var discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        dealThreeDamageToOpponent();

        resolveEndStep();

        harness.handleListChoice(player2, ChoiceContext.VillainousChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player1, "Dalek");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Does not trigger when the opponent lost less than 3 life")
    void noTriggerBelowLifeThreshold() {
        harness.addToBattlefield(player1, new DavrosDalekCreator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        resolveEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Dalek");
    }

    private void dealThreeDamageToOpponent() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
