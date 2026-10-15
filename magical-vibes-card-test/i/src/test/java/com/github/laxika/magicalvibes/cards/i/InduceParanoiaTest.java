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
        SiegeWurm.class
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
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

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
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, swiftblade.getId());

        harness.assertInGraveyard(player1, "Boros Swiftblade");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @CardUsed({SpellbreakerBehemoth.class})
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
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, wurm.getId());
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
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castAndResolveInstant(player2, 0, chord.getId());

        harness.assertInGraveyard(player1, "Chord of Calling");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 5);
    }

    @Test
    @DisplayName("The countered spell enters the graveyard before the milled cards")
    void countersBeforeMilling() {
        BorosSwiftblade swiftblade = new BorosSwiftblade();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(swiftblade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(first, second));
        prepareInduceWithBlackMana();

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, swiftblade.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isSameAs(swiftblade);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("A single black mana spent is sufficient to mill")
    void oneBlackManaIsEnough() {
        BorosSwiftblade swiftblade = new BorosSwiftblade();
        harness.setHand(player1, List.of(swiftblade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, swiftblade.getId());

        harness.assertInGraveyard(player1, "Boros Swiftblade");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mills the remaining library when it is smaller than the spell's mana value")
    void millsShortLibrary() {
        BelltowerSphinx sphinx = new BelltowerSphinx();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(sphinx));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLibrary(player1, List.of(forest));
        prepareInduceWithBlackMana();

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        harness.assertInGraveyard(player1, "Belltower Sphinx");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Does not mill when its target has already left the stack")
    void missingTargetDoesNotMill() {
        BorosSwiftblade swiftblade = new BorosSwiftblade();
        harness.setHand(player1, List.of(swiftblade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new InduceParanoia(), new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, swiftblade.getId());
        harness.castAndResolveInstant(player2, 0, swiftblade.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boros Swiftblade");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private void prepareInduceWithBlackMana() {
        harness.setHand(player2, List.of(new InduceParanoia()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
    }
}
