package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfOaks.class, GiantCockroach.class, Crawlspace.class})
class MightOfOaksTest extends BaseCardTest {


    private Permanent setupCockroachAndMight() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        return cockroach;
    }

    @Test
    @DisplayName("Casting Might of Oaks puts it on the stack as INSTANT_SPELL with target")
    void castingPutsItOnStack() {
        Permanent cockroach = setupCockroachAndMight();

        harness.castInstant(player1, 0, cockroach.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(cockroach.getId());
    }

    @Test
    @DisplayName("Resolving Might of Oaks gives +7/+7 to target creature")
    void resolvingGivesBoost() {
        Permanent cockroach = setupCockroachAndMight();

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        assertThat(cockroach.getEffectivePower()).isEqualTo(11);
        assertThat(cockroach.getEffectiveToughness()).isEqualTo(9);
        assertThat(cockroach.getPowerModifier()).isEqualTo(7);
        assertThat(cockroach.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent cockroach = setupCockroachAndMight();

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cockroach.getPowerModifier()).isEqualTo(0);
        assertThat(cockroach.getToughnessModifier()).isEqualTo(0);
        assertThat(cockroach.getEffectivePower()).isEqualTo(4);
        assertThat(cockroach.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can cast at instant speed when not the active player")
    void canCastAtInstantSpeedAsNonActivePlayer() {
        harness.forceActivePlayer(player2);
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player2 is active, passes priority to player1
        harness.passPriority(player2);

        harness.castInstant(player1, 0, cockroach.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getTargetId()).isEqualTo(cockroach.getId());
    }

    @Test
    @DisplayName("Can cast with non-empty stack")
    void canCastWithNonEmptyStack() {
        // Put a creature on the stack first
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);

        // Now player2 should be able to cast Might of Oaks in response
        Permanent cockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player2, List.of(new MightOfOaks()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, cockroach.getId());

        assertThat(harness.getGameData().stack).hasSize(2);
        assertThat(harness.getGameData().stack.getLast().getTargetId()).isEqualTo(cockroach.getId());
    }

    @Test
    @DisplayName("Spell fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent cockroach = setupCockroachAndMight();

        harness.castInstant(player1, 0, cockroach.getId());

        // Remove the creature before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle — no crash, stack should be empty
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Boosted creature in combat deals extra damage")
    void boostedCreatureDealsExtraDamage() {
        Permanent cockroach = addCreatureReady(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        // Cast and resolve Might of Oaks
        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        // Move to declare attackers
        declareAttackers(List.of(0));

        // Opponent life should be reduced by 11 (boosted power)
        int opponentLife = harness.getGameData().playerLifeTotals.get(player2.getId());
        assertThat(opponentLife).isEqualTo(20 - 11);
    }

    @Test
    @DisplayName("Multiple Might of Oaks stack additively")
    void multiplePumpsStack() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new MightOfOaks(), new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        assertThat(cockroach.getEffectivePower()).isEqualTo(18);
        assertThat(cockroach.getEffectiveToughness()).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, cockroach.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with invalid target permanent ID")
    void cannotCastWithInvalidTarget() {
        harness.addToBattlefield(player1, new GiantCockroach()); // valid target so spell is playable
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, cockroach.getId());

        assertThat(cockroach.getPowerModifier()).isEqualTo(7);
        assertThat(cockroach.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GiantCockroach()); // valid creature target keeps the spell playable
        Permanent crawlspace = harness.addToBattlefieldAndReturn(player1, new Crawlspace());
        harness.setHand(player1, List.of(new MightOfOaks()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crawlspace.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}


