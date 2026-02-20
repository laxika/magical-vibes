package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpiketailHatchlingTest {

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
    @DisplayName("Spiketail Hatchling has correct card properties")
    void hasCorrectProperties() {
        SpiketailHatchling card = new SpiketailHatchling();

        assertThat(card.getName()).isEqualTo("Spiketail Hatchling");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.DRAKE);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isNeedsSpellTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().get(1)).isInstanceOf(CounterUnlessPaysEffect.class);
        assertThat(((CounterUnlessPaysEffect) card.getActivatedAbilities().getFirst().getEffects().get(1)).amount()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Spiketail Hatchling puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new SpiketailHatchling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Spiketail Hatchling");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spiketail Hatchling"));
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability sacrifices Spiketail Hatchling and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        // Player 1 (active player) casts a creature spell
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        // Player 2 activates Spiketail Hatchling's ability targeting the spell
        harness.activateAbility(player2, 0, null, elves.getId());

        GameData gd = harness.getGameData();

        // Spiketail Hatchling should be sacrificed (not on battlefield, in graveyard)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spiketail Hatchling"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spiketail Hatchling"));

        // Ability should be on the stack above the creature spell
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Spiketail Hatchling");
    }

    // ===== Counter-unless-pays: opponent cannot pay =====

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, elves.getId());

        // Resolve the ability — player1 has no mana left, spell is countered immediately
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Elves should be countered (in player1's graveyard, not on battlefield)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));

        // Stack should be empty
        assertThat(gd.stack).isEmpty();
    }

    // ===== Counter-unless-pays: opponent pays =====

    @Test
    @DisplayName("Spell is not countered when opponent pays {1}")
    void spellNotCounteredWhenOpponentPays() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 to pay

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, elves.getId());

        // Resolve the ability — player1 has mana, gets prompted
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());

        // Player1 pays
        harness.handleMayAbilityChosen(player1, true);

        // Elves should still be on the stack (it hasn't resolved yet), not countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));

        // Resolve the elves spell
        harness.passBothPriorities();

        // Elves should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Counter-unless-pays: opponent declines to pay =====

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 available

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, elves.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());

        // Player1 declines
        harness.handleMayAbilityChosen(player1, false);

        // Spell should be countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target spell is removed from the stack")
    void fizzlesIfTargetSpellRemoved() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, elves.getId());

        // Remove target spell from the stack before ability resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        // Ability fizzles
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without a spell on the stack")
    void cannotActivateWithoutSpellTarget() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player1, hatchling);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability on the stack")
    void cannotTargetActivatedAbility() {
        SpiketailHatchling hatchling1 = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling1);

        SpiketailHatchling hatchling2 = new SpiketailHatchling();
        harness.addToBattlefield(player1, hatchling2);

        // Player1 casts a creature to put a spell on the stack
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        // Player2 activates their hatchling's ability targeting the bears spell
        harness.activateAbility(player2, 0, null, bears.getId());

        GameData gd = harness.getGameData();
        // Now there's an activated ability on the stack
        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        // Get the activated ability's card ID
        var abilityEntry = gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY)
                .findFirst().orElseThrow();

        // Player1 tries to target the activated ability with their hatchling — should fail
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityEntry.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Mana payment confirmation =====

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying {1}")
    void manaPoolReducedAfterPaying() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, elves.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player1 should have 1 green mana left (2 added - 1 to cast)
        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        // After paying, player1 should have 0 mana
        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0);
    }

    // ===== Counter own spell =====

    @Test
    @DisplayName("Can counter own controller's spell on the stack")
    void canCounterOwnSpell() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player1, hatchling);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Player 1 activates hatchling targeting their own spell
        harness.activateAbility(player1, 0, null, bears.getId());

        // Resolve — player 1 has 0 mana left, cannot pay
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}

