package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DAvenantTrapperTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("D'Avenant Trapper has historic spell-cast trigger with tap target permanent effect and target filter")
    void hasCorrectStructure() {
        DAvenantTrapper card = new DAvenantTrapper();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.targetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);

        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(TapTargetPermanentEffect.class);
    }

    // ===== Artifact spell triggers target selection =====

    @Test
    @DisplayName("Casting an artifact triggers target selection for opponent's creature")
    void artifactSpellTriggersTargetSelection() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Tap resolves correctly =====

    @Test
    @DisplayName("Choosing opponent's creature as target taps it when the triggered ability resolves")
    void tapOpponentCreature() {
        harness.addToBattlefield(player1, new DAvenantTrapper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Choose opponent's creature as target
        harness.handlePermanentChosen(player1, bearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
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
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
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
}
