package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LostInTheMistTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Lost in the Mist has counter and bounce spell effects")
    void hasCorrectProperties() {
        LostInTheMist card = new LostInTheMist();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CounterSpellEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ReturnTargetPermanentToHandEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting both a spell and a permanent")
    void castingPutsOnStackWithBothTargets() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Spellbook spellbook = new Spellbook();
        harness.addToBattlefield(player1, spellbook);
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), spellbookId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry lostEntry = gd.stack.getLast();
        assertThat(lostEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(lostEntry.getCard().getName()).isEqualTo("Lost in the Mist");
        assertThat(lostEntry.getTargetId()).isEqualTo(bears.getId());
        assertThat(lostEntry.getTargetIds()).containsExactly(spellbookId);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters the spell and bounces the permanent")
    void countersSpellAndBouncesPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Spellbook spellbook = new Spellbook();
        harness.addToBattlefield(player1, spellbook);
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), spellbookId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell was countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Permanent was bounced
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Can bounce own permanent while countering opponent's spell")
    void canBounceOwnPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Spellbook spellbook = new Spellbook();
        harness.addToBattlefield(player2, spellbook);
        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), spellbookId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Spell was countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Own permanent was bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Lost in the Mist goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, target);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lost in the Mist"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Partial fizzle =====

    @Test
    @DisplayName("Still bounces permanent if spell target is no longer on the stack")
    void stillBouncesIfSpellTargetGone() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Spellbook spellbook = new Spellbook();
        harness.addToBattlefield(player1, spellbook);
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), spellbookId);

        // Remove the spell target from the stack before resolution
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Spell does NOT fizzle — permanent target is still legal
        assertThat(gd.gameLog).noneMatch(log -> log.contains("fizzles"));

        // Permanent was still bounced
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Still counters spell if permanent target is no longer on the battlefield")
    void stillCountersIfPermanentTargetGone() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Spellbook spellbook = new Spellbook();
        harness.addToBattlefield(player1, spellbook);
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), spellbookId);

        // Remove the permanent target from the battlefield before resolution
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Spell does NOT fizzle — spell target is still legal
        assertThat(gd.gameLog).noneMatch(log -> log.contains("fizzles"));

        // Spell was still countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Full fizzle =====

    @Test
    @DisplayName("Fizzles if both targets are illegal")
    void fizzlesIfBothTargetsIllegal() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Spellbook spellbook = new Spellbook();
        harness.addToBattlefield(player1, spellbook);
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new LostInTheMist()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId(), spellbookId);

        // Remove both targets before resolution
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Entire spell fizzles
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));

        // Lost in the Mist goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lost in the Mist"));
    }
}
