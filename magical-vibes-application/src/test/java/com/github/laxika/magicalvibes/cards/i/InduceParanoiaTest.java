package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BelltowerSphinx;
import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.c.ChordOfCalling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        InduceParanoia.class,
        BelltowerSphinx.class,
        BorosSwiftblade.class,
        ChordOfCalling.class,
        Forest.class,
        SiegeWurm.class,
        SpellbreakerBehemoth.class
})
class InduceParanoiaTest extends BaseCardTest {

    @Test
    @DisplayName("With black mana spent, counters the spell and mills its mana value")
    void blackManaMillsTargetSpellManaValue() {
        BelltowerSphinx sphinx = new BelltowerSphinx();
        harness.setHand(player1, List.of(sphinx));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        prepareInduceWithBlackMana();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sphinx.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Belltower Sphinx");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 5);
    }

    @Test
    @DisplayName("Without black mana, counters the spell without milling")
    void noBlackManaDoesNotMill() {
        BorosSwiftblade swiftblade = new BorosSwiftblade();
        harness.setHand(player1, List.of(swiftblade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player2, List.of(new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, swiftblade.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boros Swiftblade");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("With black mana spent, mills even when the target cannot be countered")
    void blackManaMillsUncounterableSpell() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());

        SiegeWurm wurm = new SiegeWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        prepareInduceWithBlackMana();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wurm.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Siege Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 7);
    }

    @Test
    @DisplayName("With black mana spent, mills an X spell's chosen mana value")
    void blackManaMillsChosenXSpellManaValue() {
        ChordOfCalling chord = new ChordOfCalling();
        harness.setHand(player1, List.of(chord));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        prepareInduceWithBlackMana();
        harness.castInstant(player1, 0, 2, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chord.getId());

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chord of Calling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 5);
    }

    private void prepareInduceWithBlackMana() {
        harness.setHand(player2, List.of(new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
    }
}
