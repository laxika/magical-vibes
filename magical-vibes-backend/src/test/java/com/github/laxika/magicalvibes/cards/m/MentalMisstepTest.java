package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MentalMisstepTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mental Misstep has correct card properties")
    void hasCorrectProperties() {
        MentalMisstep card = new MentalMisstep();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryManaValuePredicate(1),
                "Target spell must have mana value 1."
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CounterSpellEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Can target a creature spell with mana value 1")
    void canTargetManaValue1CreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry misstepEntry = gd.stack.getLast();
        assertThat(misstepEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(misstepEntry.getCard().getName()).isEqualTo("Mental Misstep");
        assertThat(misstepEntry.getTargetId()).isEqualTo(elves.getId());
    }

    @Test
    @DisplayName("Can target an instant spell with mana value 1")
    void canTargetManaValue1InstantSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry misstepEntry = gd.stack.getLast();
        assertThat(misstepEntry.getCard().getName()).isEqualTo("Mental Misstep");
        assertThat(misstepEntry.getTargetId()).isEqualTo(shock.getId());
    }

    @Test
    @DisplayName("Cannot target a spell with mana value 2")
    void cannotTargetManaValue2Spell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters a mana value 1 creature spell")
    void countersManaValue1CreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Mental Misstep goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mental Misstep"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Phyrexian mana =====

    @Test
    @DisplayName("Can be cast by paying 2 life instead of blue mana")
    void canBeCastWithPhyrexianMana() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        // No blue mana — will pay with life

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered spell goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        // Player 2 paid 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MentalMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mental Misstep"));
    }
}
