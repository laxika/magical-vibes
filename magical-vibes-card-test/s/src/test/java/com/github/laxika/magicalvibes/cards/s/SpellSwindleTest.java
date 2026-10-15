package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellSwindle.class, GrizzlyBears.class, SerraAngel.class, HangarbackWalker.class, CarnageTyrant.class})
class SpellSwindleTest extends BaseCardTest {

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
        assertThat(swindleEntry.getCard()).isInstanceOf(SpellSwindle.class);
        assertThat(swindleEntry.getTargetId()).isEqualTo(bears.getId());
    }

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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        // Spell is countered — Bears goes to graveyard, not battlefield
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // 2 Treasure tokens created for the counter spell's controller
        List<Permanent> treasures = findPermanents(player2, "Treasure");
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
        harness.castAndResolveInstant(player2, 0, angel.getId());

        // Spell is countered
        harness.assertInGraveyard(player1, "Serra Angel");

        // 5 Treasure tokens created
        List<Permanent> treasures = findPermanents(player2, "Treasure");
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
        harness.castAndResolveInstant(player2, 0, bears.getId());

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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(findPermanents(player2, "Treasure")).isEmpty();

        // Spell Swindle still goes to caster's graveyard
        harness.assertInGraveyard(player2, "Spell Swindle");
    }

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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Spell Swindle");
        assertThat(gd.stack).isEmpty();
    }

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
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("countered"));
    }

    @Test
    @CardUsed({HangarbackWalker.class})
    void countsEveryXSymbolInTargetManaCost() {
        HangarbackWalker walker = new HangarbackWalker();
        harness.setHand(player1, List.of(walker));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castArtifact(player1, 0, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, walker.getId());

        harness.assertInGraveyard(player1, "Hangarback Walker");
        assertThat(findPermanents(player2, "Treasure")).hasSize(6);
    }

    @Test
    @CardUsed({CarnageTyrant.class})
    void createsTreasuresEvenWhenTargetCannotBeCountered() {
        CarnageTyrant tyrant = new CarnageTyrant();
        harness.setHand(player1, List.of(tyrant));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tyrant.getId());

        assertThat(findPermanents(player2, "Treasure")).hasSize(6);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Carnage Tyrant");
    }

    @Test
    @CardUsed({HangarbackWalker.class})
    void createsNoTreasuresForZeroManaValueSpell() {
        HangarbackWalker walker = new HangarbackWalker();
        harness.setHand(player1, List.of(walker));
        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castArtifact(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, walker.getId());

        harness.assertInGraveyard(player1, "Hangarback Walker");
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}
