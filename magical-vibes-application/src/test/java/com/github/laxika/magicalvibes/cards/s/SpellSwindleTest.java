package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndCreateTreasureTokensEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpellSwindleTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has counter + treasure creation effect on SPELL slot")
    void hasCorrectEffectStructure() {
        SpellSwindle card = new SpellSwindle();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CounterSpellAndCreateTreasureTokensEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts Spell Swindle on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry swindleEntry = gd.stack.getLast();
        assertThat(swindleEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(swindleEntry.getCard().getName()).isEqualTo("Spell Swindle");
        assertThat(swindleEntry.getTargetId()).isEqualTo(bears.getId());
    }

    // ===== Countering + Treasure creation =====

    @Test
    @DisplayName("Counters target spell and creates Treasure tokens equal to its mana value (MV 2)")
    void countersSpellAndCreatesTreasuresEqualToManaValue() {
        GrizzlyBears bears = new GrizzlyBears(); // {1}{G} = MV 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Spell is countered — Bears goes to graveyard, not battlefield
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // 2 Treasure tokens created for the counter spell's controller
        List<Permanent> treasures = findAllPermanents(player2, "Treasure");
        assertThat(treasures).hasSize(2);
    }

    @Test
    @DisplayName("Counters a 5-mana spell and creates 5 Treasure tokens")
    void countersHighManaValueSpellAndCreatesCorrectTreasures() {
        SerraAngel angel = new SerraAngel(); // {3}{W}{W} = MV 5
        harness.setHand(player1, List.of(angel));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Spell is countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));

        // 5 Treasure tokens created
        List<Permanent> treasures = findAllPermanents(player2, "Treasure");
        assertThat(treasures).hasSize(5);
    }

    @Test
    @DisplayName("Treasure tokens are artifact tokens with Treasure subtype")
    void treasureTokensAreCorrectType() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player2, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure tokens have sacrifice-for-mana activated ability")
    void treasureTokensHaveManaAbility() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player2, "Treasure");
        assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(treasure.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles entirely if target spell is no longer on the stack — no Treasures")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        // Remove Bears from stack before Spell Swindle resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Entire spell fizzles — no counter, no treasures
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(findAllPermanents(player2, "Treasure")).isEmpty();

        // Spell Swindle still goes to caster's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spell Swindle"));
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Spell Swindle goes to caster's graveyard after resolving")
    void spellSwindleGoesToGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spell Swindle"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records the counter")
    void gameLogRecordsCounter() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog)
                .anyMatch(log -> log.contains("Grizzly Bears") && log.contains("countered"));
    }

    // ===== Helpers =====


    private List<Permanent> findAllPermanents(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .toList();
    }
}
