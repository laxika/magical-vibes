package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WretchedCamel.class, GrizzlyBears.class, Shock.class, SunscorchedDesert.class})
class WretchedCamelTest extends BaseCardTest {

    @Test
    @DisplayName("Dies while controlling a Desert — target player discards a card")
    void diesWithDesertOnBattlefieldForcesDiscard() {
        harness.addToBattlefield(player1, new WretchedCamel());
        harness.addToBattlefield(player1, new SunscorchedDesert());

        killCamel();

        // Player1 controls the death trigger and chooses which player discards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve discard trigger → target chooses a card

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dies with a Desert card in the graveyard — target player discards a card")
    void diesWithDesertInGraveyardForcesDiscard() {
        harness.addToBattlefield(player1, new WretchedCamel());
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));

        killCamel();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("\"Target player\" lets the controller choose themselves to discard")
    void controllerMayTargetSelf() {
        harness.addToBattlefield(player1, new WretchedCamel());
        harness.addToBattlefield(player1, new SunscorchedDesert());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        killCamel();

        // The controller (player1) is a legal target for "target player".
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dies without any Desert — the intervening-if ability does not trigger")
    void diesWithoutDesertDiscardsNothing() {
        harness.addToBattlefield(player1, new WretchedCamel());

        killCamel();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void opponentsDesertsDoNotEnableTrigger() {
        harness.addToBattlefield(player1, new WretchedCamel());
        harness.addToBattlefield(player2, new SunscorchedDesert());
        harness.setGraveyard(player2, List.of(new SunscorchedDesert()));

        killCamel();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void desertConditionIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new WretchedCamel());
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        killCamel();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setGraveyard(player1, List.of(new WretchedCamel()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void targetWithEmptyHandDoesNotDiscard() {
        harness.addToBattlefield(player1, new WretchedCamel());
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        killCamel();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    /** Player2 becomes active and Shocks the camel to death, leaving one card in player2's hand. */
    private void killCamel() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID camelId = harness.getPermanentId(player1, "Wretched Camel");
        harness.castAndResolveInstant(player2, 0, camelId); // Shock resolves → camel dies → death trigger awaits target
    }
}
