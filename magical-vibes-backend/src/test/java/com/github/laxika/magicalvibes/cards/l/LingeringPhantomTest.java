package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LingeringPhantomTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has graveyard spell-cast trigger with historic filter and {B} mana cost")
    void hasCorrectStructure() {
        LingeringPhantom card = new LingeringPhantom();

        assertThat(card.getEffects(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);

        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect)
                card.getEffects(EffectSlot.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(trigger.manaCost()).isEqualTo("{B}");
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Trigger from graveyard on historic spell cast =====

    @Test
    @DisplayName("Casting an artifact triggers may-pay prompt when Lingering Phantom is in graveyard")
    void artifactCastTriggersMayPayPrompt() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        // Stack: [Spellbook (bottom), MayPayMana trigger (top)]
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may-pay and paying {B} returns Lingering Phantom from graveyard to hand")
    void acceptAndPayReturnsToHand() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phantom.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(phantom.getId()));
        // Black mana spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining may-pay keeps Lingering Phantom in graveyard")
    void declineKeepsInGraveyard() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(phantom.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phantom.getId()));
    }

    @Test
    @DisplayName("Casting a legendary creature triggers the graveyard ability")
    void legendaryCastTriggers() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phantom.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(phantom.getId()));
    }

    @Test
    @DisplayName("Casting a non-historic spell does not trigger the graveyard ability")
    void nonHistoricDoesNotTrigger() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phantom.getId()));
    }

    @Test
    @DisplayName("Opponent casting a historic spell does not trigger controller's graveyard phantom")
    void opponentHistoricDoesNotTrigger() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        // Only the artifact spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phantom.getId()));
    }

    @Test
    @DisplayName("Does not trigger when Lingering Phantom is on the battlefield")
    void doesNotTriggerFromBattlefield() {
        harness.addToBattlefield(player1, new LingeringPhantom());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        // Only the artifact spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Accepting may-pay without enough mana treats as decline")
    void acceptWithoutManaIsDecline() {
        LingeringPhantom phantom = new LingeringPhantom();
        harness.setGraveyard(player1, List.of(phantom));
        harness.setHand(player1, List.of(new Spellbook()));
        // No black mana added

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve MayPayMana trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept without mana

        // Phantom should remain in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(phantom.getId()));
    }
}
