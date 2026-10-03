package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

@CardUsed({CleansingNova.class, GloriousAnthem.class, GrizzlyBears.class, JayemdaeTome.class,
        Ornithopter.class, TrollAscetic.class})
class CleansingNovaTest extends BaseCardTest {

    private void castNova(final int mode) {
        harness.setHand(player1, List.of(new CleansingNova()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, mode);
    }

    @Test
    @DisplayName("First mode destroys all creatures on both sides, leaving noncreature permanents")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.addToBattlefield(player1, new GloriousAnthem());

        castNova(0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Jayemdae Tome");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Second mode destroys artifacts and enchantments, sparing nonartifact creatures")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castNova(1);

        harness.assertNotOnBattlefield(player1, "Jayemdae Tome");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode can resolve with an empty battlefield")
    void resolvesWithEmptyBattlefield(int mode) {
        castNova(mode);

        harness.assertInGraveyard(player1, "Cleansing Nova");
    }

    @Test
    @DisplayName("Creature mode destroys an opposing hexproof creature")
    void destroysOpposingHexproofCreature() {
        harness.addToBattlefield(player2, new TrollAscetic());

        castNova(0);

        harness.assertNotOnBattlefield(player2, "Troll Ascetic");
        harness.assertInGraveyard(player2, "Troll Ascetic");
    }

    @Test
    @DisplayName("A regeneration shield saves a creature from creature mode")
    void allowsRegeneration() {
        harness.addToBattlefield(player1, new TrollAscetic());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        castNova(0);

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        harness.assertNotInGraveyard(player1, "Troll Ascetic");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
