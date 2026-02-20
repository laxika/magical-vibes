package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelsFeatherTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Angel's Feather has correct card properties")
    void hasCorrectProperties() {
        AngelsFeather card = new AngelsFeather();

        assertThat(card.getName()).isEqualTo("Angel's Feather");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{2}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(GainLifeOnColorSpellCastEffect.class);
        GainLifeOnColorSpellCastEffect effect = (GainLifeOnColorSpellCastEffect) mayEffect.wrapped();
        assertThat(effect.triggerColor()).isEqualTo(CardColor.WHITE);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Angel's Feather puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new AngelsFeather()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Angel's Feather");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Angel's Feather resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new AngelsFeather()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angel's Feather"));
    }

    // ===== Triggered ability: controller casts white spell =====

    @Test
    @DisplayName("Controller casts white spell, accepts may ability, gains 1 life")
    void controllerCastsWhiteSpellAndAccepts() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Player1 should be prompted for may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Angel's Feather"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts white spell, declines may ability, no life gain")
    void controllerCastsWhiteSpellAndDeclines() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Angel's Feather"));

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Triggered ability: opponent casts white spell =====

    @Test
    @DisplayName("Opponent casts white spell, controller accepts may ability, gains 1 life")
    void opponentCastsWhiteSpellControllerAccepts() {
        harness.addToBattlefield(player1, new AngelsFeather());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Player1 (controller of Angel's Feather) should be prompted
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Non-white spell does NOT trigger =====

    @Test
    @DisplayName("Non-white spell does not trigger Angel's Feather")
    void nonWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Multiple feathers =====

    @Test
    @DisplayName("Multiple Angel's Feathers each trigger independently")
    void multipleFeathersTriggerIndependently() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // First feather prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second feather prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve all
        harness.passBothPriorities(); // resolve second triggered ability
        harness.passBothPriorities(); // resolve first triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== No trigger when not on battlefield =====

    @Test
    @DisplayName("Angel's Feather does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Angel's Feather is in the hand, not on the battlefield
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}


