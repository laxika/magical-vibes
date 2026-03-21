package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyncopateTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Syncopate has correct card properties")
    void hasCorrectProperties() {
        Syncopate card = new Syncopate();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isNull();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        CounterUnlessPaysEffect effect = (CounterUnlessPaysEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.useXValue()).isTrue();
        assertThat(effect.exileIfCountered()).isTrue();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 3); // 1U + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        var syncopateEntry = gd.stack.getLast();
        assertThat(syncopateEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(syncopateEntry.getCard().getName()).isEqualTo("Syncopate");
        assertThat(syncopateEntry.getTargetId()).isEqualTo(bears.getId());
    }

    // ===== Counter + exile: opponent cannot pay =====

    @Test
    @DisplayName("Counters and exiles spell when opponent has no mana to pay X")
    void countersAndExilesWhenOpponentCannotPay() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2); // 1U + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, elves.getId());

        // Resolve — player1 has 0 mana, spell is countered and exiled immediately
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell should be exiled, NOT in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Counter + exile: opponent declines to pay =====

    @Test
    @DisplayName("Counters and exiles spell when opponent declines to pay")
    void countersAndExilesWhenOpponentDeclines() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 available

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2); // 1U + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Player1 declines to pay
        harness.handleMayAbilityChosen(player1, false);

        // Spell should be exiled, NOT in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    // ===== Opponent pays: spell not countered =====

    @Test
    @DisplayName("Spell is not countered when opponent pays X")
    void spellNotCounteredWhenOpponentPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 3); // 1 to cast, 2 to pay X=2

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 3); // 1U + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Player1 pays {2}
        harness.handleMayAbilityChosen(player1, true);

        // Elves should not be countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));

        // Resolve the elves spell
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    // ===== Mana payment confirmation =====

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying X")
    void manaPoolReducedAfterPaying() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 4); // 1 to cast, 3 to pay X=3

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 4); // 1U + X=3

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(3); // 4 added - 1 to cast

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0); // 3 - 3 paid
    }

    // ===== X=0 counters immediately and exiles =====

    @Test
    @DisplayName("X=0 counters and exiles immediately (opponent cannot pay 0)")
    void xEqualsZeroCountersAndExilesImmediately() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 1); // U + X=0

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, elves.getId());

        // Resolve — X=0, opponent can always pay {0}, so may-ability is prompted
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // With X=0, ManaCost("{0}") can always be paid, so player is asked
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Player1 declines to pay {0} (silly but valid)
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 3); // 1U + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Syncopate"));
    }

    // ===== Syncopate goes to graveyard =====

    @Test
    @DisplayName("Syncopate goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2); // 1U + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Syncopate"));
    }
}
