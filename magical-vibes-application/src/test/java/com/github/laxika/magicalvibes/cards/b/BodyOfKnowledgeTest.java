package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyOfKnowledge.class, GrizzlyBears.class, Shock.class})
class BodyOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the controller's hand size")
    void ptEqualsControllerHandSize() {
        Permanent body = harness.addToBattlefieldAndReturn(player1, new BodyOfKnowledge());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, body)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, body)).isEqualTo(3);

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, body)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, body)).isEqualTo(4);
    }

    @Test
    @DisplayName("Draws the amount of damage dealt to it")
    void drawsCardsEqualToDamageTaken() {
        Permanent body = harness.addToBattlefieldAndReturn(player1, new BodyOfKnowledge());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, body.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gqs.getEffectivePower(gd, body)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, body)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Body of Knowledge");
    }

    @Test
    @DisplayName("Controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new BodyOfKnowledge());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }
}
