package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DAvenantTrapper.class, AdelizTheCinderWind.class, GrizzlyBears.class,
        Spellbook.class, HistoryOfBenalia.class})
class DAvenantTrapperTest extends BaseCardTest {

    // ===== Artifact spell triggers target selection =====

    @Test
    @DisplayName("Casting an artifact triggers target selection for opponent's creature")
    void artifactSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    // ===== Tap resolves correctly =====

    @Test
    @DisplayName("Choosing opponent's creature as target taps it when the triggered ability resolves")
    void tapOpponentCreature() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Choose opponent's creature as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    // ===== Legendary spell triggers =====

    @Test
    @DisplayName("Casting a legendary creature triggers target selection")
    void legendarySpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    // ===== Non-historic does not trigger =====

    @Test
    @DisplayName("Casting a non-historic creature does not trigger the ability")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Opponent's historic spell does not trigger =====

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's D'Avenant Trapper")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== No valid targets — trigger is skipped =====

    @Test
    @DisplayName("Trigger is skipped when opponent has no creatures")
    void triggerSkippedWhenNoValidTargets() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        // No creatures on opponent's battlefield
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Spellbook on stack, no triggered ability (trigger skipped due to no valid targets)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Controller's own creatures are not valid targets =====

    @Test
    @DisplayName("Controller's own creatures are not valid targets")
    void ownCreaturesNotValidTargets() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        // Only controller's creatures on battlefield (no opponent creatures)
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Trigger skipped — controller's creatures are not valid targets
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Casting a Saga taps the target before the Saga resolves")
    void sagaSpellTriggers() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DAvenantTrapper());
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "History of Benalia");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void alreadyTappedCreatureIsLegalTarget() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DAvenantTrapper());
        target.tap();
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The triggered ability resolves after its source leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DAvenantTrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DAvenantTrapper());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A target that comes under your control is not tapped on resolution")
    void targetMustStillBeControlledByOpponent() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DAvenantTrapper());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }
}
