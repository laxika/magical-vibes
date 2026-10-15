package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

        harness.castAndResolveInstant(player2, 0, body.getId());
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

    @Test
    @DisplayName("Lethal damage kills Body of Knowledge before its draw trigger resolves")
    void lethalDamageStillDrawsFullDamageAmount() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Permanent body = harness.addToBattlefieldAndReturn(player1, new BodyOfKnowledge());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, body.getId());

        harness.assertNotOnBattlefield(player1, "Body of Knowledge");
        harness.assertInGraveyard(player1, "Body of Knowledge");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Body of Knowledge");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent still discards to maximum hand size")
    void opponentStillHasMaximumHandSize() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new BodyOfKnowledge());
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Casting Body of Knowledge with no other cards in hand makes it die")
    void diesWithEmptyHandAfterCasting() {
        harness.setHand(player1, List.of(new BodyOfKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Body of Knowledge");
        harness.assertInGraveyard(player1, "Body of Knowledge");
    }

    @Test
    @DisplayName("Combat damage draws cards for the damaged creature's controller")
    void combatDamageDrawsCards() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new GrizzlyBears());
        Permanent body = addCreatureReady(player2, new BodyOfKnowledge());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gqs.getEffectivePower(gd, body)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, body)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Body of Knowledge");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The power and toughness ability counts itself in hand and applies in the graveyard")
    void characteristicAbilityAppliesOutsideBattlefield() {
        BodyOfKnowledge body = new BodyOfKnowledge();
        harness.setHand(player1, List.of(body, new GrizzlyBears()));

        assertThat(gqs.getEffectiveCardPower(gd, body)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, body)).isEqualTo(2);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.playerGraveyards.get(player1.getId()).add(body);

        assertThat(gqs.getEffectiveCardPower(gd, body)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, body)).isEqualTo(1);
    }
}
