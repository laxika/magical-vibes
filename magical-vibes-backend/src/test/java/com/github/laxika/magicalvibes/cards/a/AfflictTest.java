package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AfflictTest {

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

    private void setupBearAndAfflict() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Casting Afflict puts it on the stack as INSTANT_SPELL with target")
    void castingPutsItOnStack() {
        setupBearAndAfflict();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Afflict");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetPermanentId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving Afflict gives -1/-1 to target creature")
    void resolvingGivesMinusOneMinusOne() {
        setupBearAndAfflict();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving Afflict draws a card for the caster")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears deckCard = new GrizzlyBears();
        harness.getGameData().playerDecks.get(player1.getId()).add(deckCard);

        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Hand should be empty after casting (Afflict was the only card)
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        // After resolving, player should have drawn a card
        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Debuff wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        setupBearAndAfflict();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Spell fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        setupBearAndAfflict();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);

        // Remove the bear before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell should fizzle — no crash, stack should be empty
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast with invalid target permanent ID")
    void cannotCastWithInvalidTarget() {
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("Afflict goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupBearAndAfflict();
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Afflict"));
    }
}
