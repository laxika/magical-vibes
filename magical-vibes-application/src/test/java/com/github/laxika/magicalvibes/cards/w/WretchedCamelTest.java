package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SunscorchedDesert;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WretchedCamelTest extends BaseCardTest {

    // ===== Death trigger — Desert condition met =====

    @Test
    @DisplayName("Dies while controlling a Desert — target player discards a card")
    void diesWithDesertOnBattlefieldForcesDiscard() {
        harness.addToBattlefield(player1, new WretchedCamel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));

        killCamel();

        // Player1 controls the death trigger and chooses which player discards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve discard trigger → target chooses a card

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("\"Target player\" lets the controller choose themselves to discard")
    void controllerMayTargetSelf() {
        harness.addToBattlefield(player1, new WretchedCamel());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Death trigger — Desert condition not met =====

    @Test
    @DisplayName("Dies without any Desert — no card is discarded (intervening-if fails at resolution)")
    void diesWithoutDesertDiscardsNothing() {
        harness.addToBattlefield(player1, new WretchedCamel());

        killCamel();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // condition not met → discard does nothing

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    /** Player2 becomes active and Shocks the camel to death, leaving one card in player2's hand. */
    private void killCamel() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID camelId = harness.getPermanentId(player1, "Wretched Camel");
        harness.castInstant(player2, 0, camelId);
        harness.passBothPriorities(); // Shock resolves → camel dies → death trigger awaits target
    }
}
