package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TesharAncestorsApostleTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has historic spell-cast trigger with graveyard-targeting return effect")
    void hasCorrectStructure() {
        TesharAncestorsApostle card = new TesharAncestorsApostle();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);

        ReturnCardFromGraveyardEffect returnEffect = (ReturnCardFromGraveyardEffect) trigger.resolvedEffects().getFirst();
        assertThat(returnEffect.targetGraveyard()).isTrue();
    }

    // ===== Historic trigger: graveyard return =====

    @Test
    @DisplayName("Casting an artifact triggers graveyard targeting when valid creature in graveyard")
    void artifactTriggerShowsGraveyardChoice() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Should prompt for graveyard target selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Selecting a valid creature returns it to the battlefield")
    void selectingCreatureReturnsItToBattlefield() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Choose the creature from graveyard
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Teshar, Ancestor's Apostle"));

        // Resolve triggered ability
        harness.passBothPriorities();

        // Grizzly Bears should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature with MV > 3 in graveyard does not trigger (no valid targets)")
    void creatureWithHighManaValueNotValid() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        harness.setGraveyard(player1, List.of(new ThunderingGiant()));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // No valid graveyard targets → trigger skipped, no graveyard choice prompt
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Non-creature card in graveyard does not qualify as a target")
    void nonCreatureNotValid() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        // Spellbook is an artifact, not a creature
        harness.setGraveyard(player1, List.of(new Spellbook()));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // No valid graveyard targets → trigger skipped
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Empty graveyard does not trigger")
    void emptyGraveyardNoTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // No creature cards in graveyard → trigger skipped
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Non-historic spell does not trigger")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));

        // Cast a non-historic creature (Grizzly Bears is not legendary/artifact/Saga)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no graveyard choice prompt
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.pendingSpellGraveyardTargetTriggers).isEmpty();
    }

    @Test
    @DisplayName("Opponent casting historic spell does not trigger controller's Teshar")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        // No graveyard choice prompt
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("Casting a legendary creature triggers Teshar")
    void legendaryCreatureTriggers() {
        harness.addToBattlefield(player1, new TesharAncestorsApostle());
        GrizzlyBears graveyardBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardBears));

        // Cast another legendary creature (Teshar itself is legendary)
        harness.setHand(player1, List.of(new TesharAncestorsApostle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        // Should prompt for graveyard target selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    // ===== Helpers =====

    private Permanent findTeshar(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Teshar, Ancestor's Apostle"))
                .findFirst().orElse(null);
    }
}
