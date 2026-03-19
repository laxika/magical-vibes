package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DissipateTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Dissipate has correct card properties")
    void hasCorrectProperties() {
        Dissipate card = new Dissipate();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isNull();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CounterSpellAndExileEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry dissipateEntry = gd.stack.getLast();
        assertThat(dissipateEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(dissipateEntry.getCard().getName()).isEqualTo("Dissipate");
        assertThat(dissipateEntry.getTargetId()).isEqualTo(bears.getId());
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Counters a creature spell and exiles it instead of putting it in the graveyard")
    void countersCreatureSpellAndExiles() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered creature is exiled, not in graveyard
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Counters a non-creature spell and exiles it")
    void countersNonCreatureSpellAndExiles() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered spell is exiled, not in graveyard
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Might of Oaks"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Might of Oaks"));
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Might of Oaks"));
    }

    @Test
    @DisplayName("Dissipate goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Dissipate itself goes to its caster's graveyard (not exiled)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dissipate"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dissipate"));
    }
}
