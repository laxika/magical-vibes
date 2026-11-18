package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FistsOfTheAnvil.class, GrizzlyBears.class, Forest.class})
class FistsOfTheAnvilTest extends BaseCardTest {

    private void setupBearAndFists() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);
    }

    @Test
    @DisplayName("Casting Fists of the Anvil puts it on the stack as INSTANT_SPELL with target")
    void castingPutsItOnStack() {
        setupBearAndFists();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving Fists of the Anvil gives +4/+0 to target creature")
    void resolvingGivesBoost() {
        setupBearAndFists();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        setupBearAndFists();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearId);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can cast at instant speed when not the active player")
    void canCastAtInstantSpeedAsNonActivePlayer() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player2 is active, passes priority to player1
        harness.passPriority(player2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Spell fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        setupBearAndFists();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);

        // Remove the bear before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle — no crash, stack should be empty
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Boosted creature in combat deals extra damage")
    void boostedCreatureDealsExtraDamage() {
        setupBearAndFists();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        // Remove summoning sickness
        findPermanent(player1, "Grizzly Bears").setSummoningSick(false);

        // Cast and resolve Fists of the Anvil
        harness.castAndResolveInstant(player1, 0, bearId);

        // Move to declare attackers
        declareAttackers(List.of(0));

        // Opponent life should be reduced by 6 (2 base + 4 boost)
        harness.assertLife(player2, 20 - 6);
    }

    @Test
    @DisplayName("Multiple Fists of the Anvil stack additively")
    void multiplePumpsStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FistsOfTheAnvil(), new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearId);

        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(10);    // 2 + 4 + 4
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2); // unchanged
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast without red mana")
    void cannotCastWithoutRedMana() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An end-step boost expires before the next turn's upkeep")
    void endStepBoostExpiresAtCleanup() {
        setupBearAndFists();
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.withAutoStop(TurnStep.END_STEP, () -> harness.castAndResolveInstant(player1, 0, bearId));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBear)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Fists of the Anvil");

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast with invalid target permanent ID")
    void cannotCastWithInvalidTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}


