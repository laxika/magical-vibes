package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Painsmith.class, CarapaceForger.class, AccordersShield.class, Memnite.class})
class PainsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact spell requires a creature target before the optional choice")
    void artifactCastRequiresTargetBeforeMayChoice() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting gives target creature +2/+0 and deathtouch until end of turn")
    void acceptBoostsAndGrantsDeathtouch() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new CarapaceForger());
        UUID targetId = harness.getPermanentId(player1, "Carapace Forger");
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);

        // Should be prompting for target selection
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the creature target
        harness.handlePermanentChosen(player1, targetId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Painsmith"));

        // The optional choice is made during resolution.
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Carapace Forger should have +2/+0 and deathtouch
        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Declining may ability does not boost or grant deathtouch")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Carapace Forger"));

        assertThat(harness.getGameData().stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Painsmith"));

        // Carapace Forger should not be boosted
        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Non-artifact spell does not trigger Painsmith")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.setHand(player1, List.of(new CarapaceForger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting artifact does not trigger Painsmith")
    void opponentArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new Painsmith());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AccordersShield()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Boost and deathtouch reset at end of turn")
    void boostResetsAtEndOfTurn() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new CarapaceForger());
        UUID targetId = harness.getPermanentId(player1, "Carapace Forger");
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Carapace Forger");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Can target opponent's creature with the boost")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player2, new CarapaceForger());
        UUID targetId = harness.getPermanentId(player2, "Carapace Forger");
        harness.setHand(player1, List.of(new AccordersShield()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player2, "Carapace Forger");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Putting an artifact onto the battlefield does not trigger Painsmith")
    void artifactEnteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact creature spell triggers Painsmith before the creature enters")
    void artifactCreatureCastCanBoostPainsmithItself() {
        harness.addToBattlefield(player1, new Painsmith());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castCreature(player1, 0);
        assertThat(harness.getGameData().interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Painsmith"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent painsmith = findPermanent(player1, "Painsmith");
        assertThat(painsmith.getPowerModifier()).isEqualTo(2);
        assertThat(painsmith.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        harness.assertNotOnBattlefield(player1, "Memnite");
        assertThat(harness.getGameData().stack).anyMatch(e -> e.getEntryType() == StackEntryType.CREATURE_SPELL);
    }
}
