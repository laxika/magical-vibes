package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.UntapUpToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
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

class UnwindTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Unwind has correct card properties")
    void hasCorrectProperties() {
        Unwind card = new Unwind();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                ),
                "Target must be a noncreature spell."
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CounterSpellEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(UntapUpToControlledPermanentsEffect.class);

        UntapUpToControlledPermanentsEffect untapEffect =
                (UntapUpToControlledPermanentsEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(untapEffect.count()).isEqualTo(3);
        assertThat(untapEffect.filter()).isInstanceOf(PermanentIsLandPredicate.class);
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

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry unwindEntry = gd.stack.getLast();
        assertThat(unwindEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(unwindEntry.getCard().getName()).isEqualTo("Unwind");
        assertThat(unwindEntry.getTargetId()).isEqualTo(might.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters a noncreature spell and untaps up to three lands")
    void countersSpellAndUntapsLands() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        // Add 3 tapped lands for player2
        Island island1 = new Island();
        Island island2 = new Island();
        Island island3 = new Island();
        harness.addToBattlefield(player2, island1);
        harness.addToBattlefield(player2, island2);
        harness.addToBattlefield(player2, island3);

        // Tap all lands to simulate having spent mana
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .forEach(Permanent::tap);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        // Countered spell goes to owner's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Might of Oaks"));

        // All 3 islands should be untapped
        long untappedIslands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(p -> !p.isTapped())
                .count();
        assertThat(untappedIslands).isEqualTo(3);
    }

    @Test
    @DisplayName("Only untaps up to three lands even if more are tapped")
    void untapsAtMostThreeLands() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        // Add 5 tapped lands for player2
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Island());
        }

        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .forEach(Permanent::tap);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        long untappedIslands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(p -> !p.isTapped())
                .count();
        assertThat(untappedIslands).isEqualTo(3);

        long tappedIslands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedIslands).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not untap non-land permanents")
    void doesNotUntapNonLandPermanents() {
        GrizzlyBears targetBears = new GrizzlyBears();
        harness.addToBattlefield(player1, targetBears);

        // Add a creature and a land for player2
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player2, bears2);
        harness.addToBattlefield(player2, new Island());

        GameData gd = harness.getGameData();
        // Tap both the creature and the land
        gd.playerBattlefields.get(player2.getId()).forEach(Permanent::tap);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        // Island should be untapped
        Permanent island = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .findFirst().orElseThrow();
        assertThat(island.isTapped()).isFalse();

        // Creature should still be tapped
        Permanent creature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unwind goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Unwind"));
        assertThat(gd.stack).isEmpty();
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

        harness.setHand(player2, List.of(new Unwind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        // Remove target from stack before Unwind resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Might of Oaks"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Unwind still goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Unwind"));
    }
}
