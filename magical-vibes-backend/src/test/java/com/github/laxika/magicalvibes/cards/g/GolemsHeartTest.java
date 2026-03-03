package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GolemsHeartTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has may gain life on artifact cast trigger")
    void hasCorrectEffect() {
        GolemsHeart card = new GolemsHeart();

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) mayEffect.wrapped();
        assertThat(trigger.spellFilter()).isEqualTo(new CardTypePredicate(CardType.ARTIFACT));
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(GainLifeEffect.class);
    }

    // ===== Controller casts artifact spell =====

    @Test
    @DisplayName("Controller casts artifact spell, accepts may ability, gains 1 life")
    void controllerCastsArtifactAndAccepts() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);

        // Player1 should be prompted for may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Golem's Heart"));

        // Resolve the triggered ability
        harness.passBothPriorities();
        // Resolve the artifact spell
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts artifact spell, declines may ability, no life gain")
    void controllerCastsArtifactAndDeclines() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Golem's Heart"));

        // Resolve the artifact spell
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Opponent casts artifact spell =====

    @Test
    @DisplayName("Opponent casts artifact spell, controller accepts may ability, gains 1 life")
    void opponentCastsArtifactControllerAccepts() {
        harness.addToBattlefield(player1, new GolemsHeart());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player2, 0);

        // Player1 (controller of Golem's Heart) should be prompted
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the artifact spell
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Non-artifact spell does NOT trigger =====

    @Test
    @DisplayName("Non-artifact spell does not trigger Golem's Heart")
    void nonArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Should not be awaiting may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Multiple Golem's Hearts =====

    @Test
    @DisplayName("Multiple Golem's Hearts each trigger independently")
    void multipleHeartsEachTrigger() {
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.addToBattlefield(player1, new GolemsHeart());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);

        // First heart prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second heart prompt
        harness.handleMayAbilityChosen(player1, true);

        // Two triggered abilities on the stack (plus the artifact spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve all
        harness.passBothPriorities(); // resolve second triggered ability
        harness.passBothPriorities(); // resolve first triggered ability
        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== No trigger when not on battlefield =====

    @Test
    @DisplayName("Golem's Heart does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
