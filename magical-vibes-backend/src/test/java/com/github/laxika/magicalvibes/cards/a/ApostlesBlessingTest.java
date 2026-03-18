package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApostlesBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Apostle's Blessing has correct effects")
    void hasCorrectEffects() {
        ApostlesBlessing card = new ApostlesBlessing();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(GrantProtectionChoiceUntilEndOfTurnEffect.class);

        GrantProtectionChoiceUntilEndOfTurnEffect effect =
                (GrantProtectionChoiceUntilEndOfTurnEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.includeArtifacts()).isTrue();
    }

    @Test
    @DisplayName("Resolving triggers color/artifact choice, choosing a color grants protection from that color")
    void choosingColorGrantsProtection() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Spell resolves, now awaiting color choice
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)).isTrue();

        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Choosing ARTIFACT grants protection from artifacts")
    void choosingArtifactGrantsProtection() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ARTIFACT");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Can target own artifact creature")
    void canTargetOwnArtifactCreature() {
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Gold Myr");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        Permanent myr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gold Myr"))
                .findFirst().orElseThrow();
        assertThat(myr.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        opponentCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);
        // Add valid target (creature you control) so spell is playable
        harness.addToBattlefield(player1, new IronMyr());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player1.getId()).add(enchantment);
        // Add valid target (creature you control) so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    @DisplayName("Protection from color prevents combat damage from that color")
    void protectionFromColorPreventsCombatDamage() {
        // Red creature attacks, our creature has protection from red
        Permanent attacker = new Permanent(new GrizzlyBears()); // Green 2/2
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new IronMyr()); // 1/1 artifact creature
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.getProtectionFromColorsUntilEndOfTurn().add(CardColor.GREEN);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Iron Myr survives (protection prevents green damage), Grizzly Bears dies from 1 damage... wait,
        // Grizzly Bears is 2/2 so Iron Myr's 1 damage wouldn't kill it. Both survive.
        // Iron Myr's 1 damage goes through to Grizzly Bears (no protection), Grizzly Bears has 2 toughness, survives.
        // Grizzly Bears' 2 damage to Iron Myr is prevented by protection from green.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Iron Myr"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Protection from color prevents blocking by creatures of that color")
    void protectionFromColorPreventsBlocking() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromColorsUntilEndOfTurn().add(CardColor.GREEN);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears()); // Green creature
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from color is cleared at end of turn")
    void protectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        // Simulate end of turn cleanup
        bears.resetModifiers();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Can be cast by paying Phyrexian mana with life instead of white")
    void canPayPhyrexianWithLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        // Only 1 colorless mana, no white — Phyrexian {W/P} must be paid with 2 life
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        // Player paid 2 life for the Phyrexian mana
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ApostlesBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No color choice should be requested since spell fizzled
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.COLOR_CHOICE)).isFalse();
    }
}
