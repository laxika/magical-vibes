package com.github.laxika.magicalvibes.cards.d;

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
import com.github.laxika.magicalvibes.cards.b.BogardanFirefiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonsClawTest {

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
    @DisplayName("Dragon's Claw has correct card properties")
    void hasCorrectProperties() {
        DragonsClaw card = new DragonsClaw();

        assertThat(card.getName()).isEqualTo("Dragon's Claw");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{2}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(GainLifeOnColorSpellCastEffect.class);
        GainLifeOnColorSpellCastEffect effect = (GainLifeOnColorSpellCastEffect) mayEffect.wrapped();
        assertThat(effect.triggerColor()).isEqualTo(CardColor.RED);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Dragon's Claw puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DragonsClaw()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dragon's Claw");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dragon's Claw resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new DragonsClaw()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dragon's Claw"));
    }

    // ===== Triggered ability: controller casts red spell =====

    @Test
    @DisplayName("Controller casts red spell, accepts may ability, gains 1 life")
    void controllerCastsRedSpellAndAccepts() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Player1 should be prompted for may ability
        GameData gd = harness.getGameData();
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Dragon's Claw"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts red spell, declines may ability, no life gain")
    void controllerCastsRedSpellAndDeclines() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Dragon's Claw"));

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Triggered ability: opponent casts red spell =====

    @Test
    @DisplayName("Opponent casts red spell, controller accepts may ability, gains 1 life")
    void opponentCastsRedSpellControllerAccepts() {
        harness.addToBattlefield(player1, new DragonsClaw());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BogardanFirefiend()));
        harness.addMana(player2, ManaColor.RED, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Player1 (controller of Dragon's Claw) should be prompted
        GameData gd = harness.getGameData();
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Non-red spell does NOT trigger =====

    @Test
    @DisplayName("Non-red spell does not trigger Dragon's Claw")
    void nonRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.awaitingMayAbilityPlayerId).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Multiple claws =====

    @Test
    @DisplayName("Multiple Dragon's Claws each trigger independently")
    void multipleClawsTriggerIndependently() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // First claw prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second claw prompt
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
    @DisplayName("Dragon's Claw does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Dragon's Claw is in the hand, not on the battlefield
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingMayAbilityPlayerId).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
