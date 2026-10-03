package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Condemn.class, GrizzlyBears.class, PlatinumEmperion.class})
class CondemnTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Condemn targeting an attacking creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        // Add an attacking creature as valid target so spell is playable
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        // Add an attacking creature as valid target so spell is playable
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts creature on bottom of owner's library")
    void resolvingPutsCreatureOnBottomOfLibrary() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        // Creature removed from battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        // Creature NOT in graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        // Creature on bottom of library (last element)
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getLast().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Controller gains life equal to creature's toughness")
    void controllerGainsLifeEqualToToughness() {
        harness.setLife(player1, 15);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        // Grizzly Bears has 2 toughness → controller gains 2 life (15 + 2 = 17)
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Life gain accounts for toughness modifiers")
    void lifeGainAccountsForToughnessModifiers() {
        harness.setLife(player1, 10);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setToughnessModifier(3); // 2 + 3 = 5 effective toughness

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        // Effective toughness is 5 → controller gains 5 life (10 + 5 = 15)
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Condemn goes to graveyard after resolving")
    void condemnGoesToGraveyardAfterResolving() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Condemn");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Condemn fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setLife(player1, 20);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell fizzles — no life gain
        harness.assertLife(player1, 20);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Condemn still goes to graveyard
        harness.assertInGraveyard(player2, "Condemn");
    }

    @Test
    @DisplayName("Condemn fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttacking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Condemn");
    }

    @Test
    @DisplayName("Removing Platinum Emperion allows its controller to gain life")
    @CardUsed({Condemn.class, PlatinumEmperion.class})
    void lifeGainHappensAfterAttackerLeavesBattlefield() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new PlatinumEmperion());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new Condemn()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Platinum Emperion");
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(attacker.getCard());
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Condemn may target its caster's attacking creature")
    void casterCanCondemnOwnAttacker() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new Condemn()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(attacker.getCard());
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }
}

