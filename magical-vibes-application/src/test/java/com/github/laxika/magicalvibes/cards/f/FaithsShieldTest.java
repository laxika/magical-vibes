package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaithsShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Faith's Shield has correct effects")
    void hasCorrectEffects() {
        FaithsShield card = new FaithsShield();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect normal =
                (ConditionalEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(((ControllerLifeAtLeast) normal.condition()).threshold()).isEqualTo(6);
        assertThat(normal.wrapped()).isInstanceOf(GrantProtectionChoiceUntilEndOfTurnEffect.class);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect fateful =
                (ConditionalEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(((ControllerLifeAtMost) fateful.condition()).threshold()).isEqualTo(5);
        assertThat(fateful.wrapped())
                .isInstanceOf(GrantProtectionChoiceToControllerAndPermanentsUntilEndOfTurnEffect.class);
    }

    @Test
    @DisplayName("With 6+ life, only the targeted permanent gains protection from the chosen color")
    void normalModeGrantsProtectionToTargetOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)).isTrue();
        harness.handleListChoice(player1, "RED");

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent target = battlefield.stream().filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
        Permanent other = battlefield.stream().filter(p -> !p.getId().equals(targetId)).findFirst().orElseThrow();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(other.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
        // The player does not gain protection in normal mode.
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Fateful hour: you and each permanent you control gain protection from the chosen color")
    void fatefulHourGrantsProtectionToControllerAndAllPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 5);

        UUID targetId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)).isTrue();
        harness.handleListChoice(player1, "WHITE");

        // The controller gains protection from the chosen color.
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).contains(CardColor.WHITE);

        // Every permanent the controller controls gains protection from the chosen color.
        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(permanent.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        }
    }

    @Test
    @DisplayName("Fateful hour: protection from a color prevents combat damage to the protected player")
    void fatefulHourProtectionPreventsCombatDamageToPlayer() {
        // player2 has protection from red; player1 attacks with a red Hill Giant.
        gd.playerProtectionFromColorsUntilEndOfTurn
                .computeIfAbsent(player2.getId(), k -> new HashSet<>()).add(CardColor.RED);

        Permanent giant = new Permanent(new HillGiant()); // red 3/3
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Hill Giant's red combat damage is prevented; player2 stays at 20 life.
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Fateful hour: protection from a color prevents the protected player from being targeted by that color")
    void fatefulHourProtectionPreventsTargetingByColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaithsShield(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).contains(CardColor.RED);

        // A red spell can no longer target the protected player (protection blocks all sources).
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Fateful hour: the spell still requires a target permanent you control (fizzles if removed)")
    void fatefulHourStillRequiresTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution — the whole spell fizzles, even in fateful hour.
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)).isFalse();
        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.WHITE, CardColor.RED, CardColor.BLUE, CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Fateful hour: player protection is cleared at end of turn")
    void fatefulHourProtectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaithsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.get(player1.getId())).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerProtectionFromColorsUntilEndOfTurn.getOrDefault(player1.getId(), new HashSet<>()))
                .doesNotContain(CardColor.RED);
    }
}
