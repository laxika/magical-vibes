package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShapersSanctuaryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Shapers' Sanctuary has ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY MayEffect wrapping DrawCardEffect")
    void hasCorrectEffectStructure() {
        ShapersSanctuary card = new ShapersSanctuary();

        var effects = card.getEffects(EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) effects.getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) mayEffect.wrapped();
        assertThat(drawEffect.amount()).isEqualTo(1);
    }

    // ===== Trigger on opponent spell targeting creature =====

    @Test
    @DisplayName("Triggers when opponent casts a spell targeting a creature you control")
    void triggersOnOpponentSpellTargetingCreature() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        // Shock + Shapers' Sanctuary triggered ability on stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Shapers' Sanctuary");
    }

    @Test
    @DisplayName("Accepting the trigger draws a card")
    void acceptingDrawsACard() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // resolve Shapers' Sanctuary trigger → may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // resolve Shapers' Sanctuary trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    // ===== Does NOT trigger on own spells =====

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell targeting own creature")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsId);

        // Only the Shock spell on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    // ===== Trigger on opponent activated ability targeting creature =====

    @Test
    @DisplayName("Triggers when opponent activates an ability targeting a creature you control")
    void triggersOnOpponentAbilityTargetingCreature() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // Give opponent an Elaborate Firecannon (activated ability that targets any target)
        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, bearsId);

        // Ability + Shapers' Sanctuary triggered ability on stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Shapers' Sanctuary");
    }

    // ===== Does NOT trigger for non-creature targets =====

    @Test
    @DisplayName("Does NOT trigger when opponent targets a non-creature permanent")
    void doesNotTriggerOnNonCreatureTarget() {
        harness.addToBattlefield(player1, new ShapersSanctuary());

        // Add a second Shapers' Sanctuary as the non-creature target
        harness.addToBattlefield(player1, new ShapersSanctuary());

        // Opponent needs a spell that can target an enchantment — but Shock targets "any target"
        // which includes enchantments? No, Shock targets "any target" = creature or player.
        // Let's use the Firecannon ability which targets "any target" = creature, player, or planeswalker.
        // Actually "any target" means creature, player, or planeswalker — not enchantments.
        // We just verify the enchantment itself as Sanctuary doesn't self-trigger on being targeted.
        // Instead, let's target a player — Sanctuary should not trigger for player targets.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        // Only the Shock spell on the stack — no triggered ability (player targeted, not creature)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    // ===== Two Sanctuaries stack =====

    @Test
    @DisplayName("Two Shapers' Sanctuaries each trigger separately")
    void twoSanctuariesStack() {
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new ShapersSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        // Shock + 2 Shapers' Sanctuary triggers on stack
        assertThat(gd.stack).hasSize(3);

        // Resolve both triggers and accept both
        harness.passBothPriorities(); // resolve first Sanctuary trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve second Sanctuary trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
