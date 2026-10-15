package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InkfathomInfiltrator;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DireUndercurrents.class, FugitiveWizard.class, GrizzlyBears.class, ScatheZombies.class,
        InkfathomInfiltrator.class})
class DireUndercurrentsTest extends BaseCardTest {

    // ===== Blue creature enters — target player draws =====

    @Test
    @DisplayName("A blue creature entering lets the controller make target player draw a card")
    void blueCreatureEntersTargetPlayerDraws() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the blue trigger draws no card")
    void blueCreatureEntersDecline() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    // ===== Black creature enters — target player discards =====

    @Test
    @DisplayName("A black creature entering lets the controller make target player discard a card")
    void blackCreatureEntersTargetPlayerDiscards() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Target opponent chooses the card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    // ===== Off-color creature does not trigger either ability =====

    @Test
    @DisplayName("A green creature entering triggers neither the draw nor the discard ability")
    void greenCreatureEntersNoTrigger() {
        harness.addToBattlefield(player1, new DireUndercurrents());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the black trigger leaves the target player's hand unchanged")
    void blackCreatureEntersDecline() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may target themselves with the blue trigger")
    void blueCreatureCanDrawForController() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's blue and black creature triggers neither ability")
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new InkfathomInfiltrator()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Inkfathom Infiltrator");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may target themselves with the black trigger")
    void blackCreatureCanMakeControllerDiscard() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setHand(player1, List.of(new ScatheZombies(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player with an empty hand remains a legal discard target")
    void blackTriggerCanTargetEmptyHand() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A blue and black creature creates separate draw and discard triggers")
    void blueAndBlackCreatureTriggersBothAbilities() {
        harness.addToBattlefield(player1, new DireUndercurrents());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new InkfathomInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.handleMayAbilityChosen(player1, true);
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
                harness.handleCardChosen(player2, 0);
            }
        }

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
