package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CondemnTest {

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
    @DisplayName("Condemn has correct card properties")
    void hasCorrectProperties() {
        Condemn card = new Condemn();

        assertThat(card.getName()).isEqualTo("Condemn");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GainLifeEqualToTargetToughnessEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(PutTargetOnBottomOfLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Condemn targeting an attacking creature puts it on the stack")
    void castingPutsOnStack() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Condemn");
        assertThat(entry.getTargetPermanentId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts creature on bottom of owner's library")
    void resolvingPutsCreatureOnBottomOfLibrary() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Creature NOT in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // Creature on bottom of library (last element)
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getLast().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Controller gains life equal to creature's toughness")
    void controllerGainsLifeEqualToToughness() {
        harness.setLife(player1, 15);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Grizzly Bears has 2 toughness → controller gains 2 life (15 + 2 = 17)
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Life gain accounts for toughness modifiers")
    void lifeGainAccountsForToughnessModifiers() {
        harness.setLife(player1, 10);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setToughnessModifier(3); // 2 + 3 = 5 effective toughness
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Effective toughness is 5 → controller gains 5 life (10 + 5 = 15)
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Condemn goes to graveyard after resolving")
    void condemnGoesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Condemn"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Condemn fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.setLife(player1, 20);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell fizzles — no life gain
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Condemn still goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Condemn"));
    }
}
