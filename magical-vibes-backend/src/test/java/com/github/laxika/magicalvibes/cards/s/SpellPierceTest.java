package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpellPierceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Spell Pierce has correct card properties")
    void hasCorrectProperties() {
        SpellPierce card = new SpellPierce();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                ),
                "Target must be a noncreature spell."
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CounterUnlessPaysEffect.class);
        assertThat(((CounterUnlessPaysEffect) card.getEffects(EffectSlot.SPELL).getFirst()).amount()).isEqualTo(2);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a noncreature spell")
    void castingPutsOnStackTargetingNoncreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry pierceEntry = gd.stack.getLast();
        assertThat(pierceEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(pierceEntry.getCard().getName()).isEqualTo("Spell Pierce");
        assertThat(pierceEntry.getTargetId()).isEqualTo(might.getId());
    }

    // ===== Counter-unless-pays: opponent cannot pay =====

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        // Resolve — player1 has 0 mana, spell is countered immediately
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Might of Oaks"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Counter-unless-pays: opponent pays =====

    @Test
    @DisplayName("Spell is not countered when opponent pays {2}")
    void spellNotCounteredWhenOpponentPays() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 6); // 4 to cast, 2 to pay

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Player1 pays {2}
        harness.handleMayAbilityChosen(player1, true);

        // Might of Oaks should not be countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Might of Oaks"));

        // Resolve the Might of Oaks spell
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Might of Oaks"));
    }

    // ===== Counter-unless-pays: opponent declines to pay =====

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 6); // 4 to cast, 2 available

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        // Player1 declines to pay
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Might of Oaks"));
    }

    // ===== Mana payment confirmation =====

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying {2}")
    void manaPoolReducedAfterPaying() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 6); // 4 to cast, 2 to pay

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(2); // 6 added - 4 to cast

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0); // 2 - 2 paid
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        // Remove target from stack before Spell Pierce resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Might of Oaks"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spell Pierce"));
    }

    // ===== Spell Pierce goes to graveyard =====

    @Test
    @DisplayName("Spell Pierce goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SpellPierce()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spell Pierce"));
    }
}
