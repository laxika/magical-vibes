package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaleolothTest extends BaseCardTest {

    // ===== Triggers and returns a creature when a power-5+ creature enters =====

    @Test
    @DisplayName("Accepting returns a creature card from graveyard to hand when a power-5+ creature enters")
    void triggersAndReturnsCreatureWhenBigCreatureEnters() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // Craw Wurm (6/4) — power 6 >= 5 triggers Paleoloth
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Craw Wurm → trigger queued

        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // Accept → inner resolves → graveyard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the creature card in the graveyard")
    void decliningLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Craw Wurm → trigger queued
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        harness.handleMayAbilityChosen(player1, false); // Decline

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    // ===== Only creature cards qualify =====

    @Test
    @DisplayName("Accepting with only a non-creature card in graveyard returns nothing")
    void nonCreatureCardsAreNotReturnable() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new Shock()));

        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Craw Wurm → trigger queued
        harness.passBothPriorities(); // Resolve MayEffect from stack → may prompt

        harness.handleMayAbilityChosen(player1, true); // Accept — no creature to return

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(s -> s.contains("no creature cards in graveyard"));
        harness.assertInGraveyard(player1, "Shock");
    }

    // ===== Does not trigger for a creature with power below 5 =====

    @Test
    @DisplayName("Does not trigger when a creature with power below 5 enters")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // Grizzly Bears (2/2) — power 2 does not trigger
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Grizzly Bears

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Does not trigger for itself entering =====

    @Test
    @DisplayName("Does not trigger for itself entering the battlefield")
    void doesNotTriggerForItself() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        // Paleoloth (5/5) entering must not trigger its own "another creature" ability
        harness.setHand(player1, List.of(new Paleoloth()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Paleoloth

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Does not trigger for an opponent's creature =====

    @Test
    @DisplayName("Does not trigger when an opponent's power-5+ creature enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new Paleoloth());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Opponent's Craw Wurm (6/4) enters — must not trigger Paleoloth
        harness.setHand(player2, List.of(new CrawWurm()));
        harness.addMana(player2, ManaColor.GREEN, 6);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // Resolve Craw Wurm

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
