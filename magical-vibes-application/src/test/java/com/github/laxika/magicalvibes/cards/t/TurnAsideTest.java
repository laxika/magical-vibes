package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYourPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnAsideTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Turn Aside has correct card properties")
    void hasCorrectProperties() {
        TurnAside card = new TurnAside();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsYourPermanentPredicate(),
                "Target spell must target a permanent you control."
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CounterSpellEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Can target a spell that targets a permanent you control")
    void canTargetSpellTargetingYourPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new TurnAside()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 casts Shock targeting player1's creature
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        // Player1 casts Turn Aside targeting Shock (Shock targets player1's permanent)
        harness.castInstant(player1, 0, shock.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry turnAsideEntry = gd.stack.getLast();
        assertThat(turnAsideEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(turnAsideEntry.getCard().getName()).isEqualTo("Turn Aside");
        assertThat(turnAsideEntry.getTargetId()).isEqualTo(shock.getId());
    }

    @Test
    @DisplayName("Cannot target a spell that targets opponent's permanent, not yours")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new TurnAside()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        // Player1 (active player) casts Giant Growth targeting their own creature
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        // Player2 tries Turn Aside - should fail because Giant Growth targets player1's permanent, not player2's
        assertThatThrownBy(() -> harness.castInstant(player2, 0, growth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature spell (does not target a permanent)")
    void cannotTargetNonTargetingSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new TurnAside()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        // Player2 tries to cast Turn Aside targeting a creature spell that doesn't target anything
        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters the spell that targets your permanent")
    void countersSpellTargetingYourPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new TurnAside()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered spell goes to owner's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        // Creature is still alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Turn Aside goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new TurnAside()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Turn Aside"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new TurnAside()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());

        // Remove target from stack before Turn Aside resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Shock"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Turn Aside still goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Turn Aside"));
    }
}
