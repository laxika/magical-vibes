package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AdventOfTheWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.w.Willbender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GazeOfGranite.class, Forest.class, HillGiant.class, LlanowarElves.class,
        HowlingMine.class, SerraAngel.class, GrizzlyBears.class, Ornithopter.class,
        GloriousAnthem.class, TrollAscetic.class, Willbender.class, AdventOfTheWurm.class})
class GazeOfGraniteTest extends BaseCardTest {

    private void castGaze(int xValue) {
        harness.setHand(player1, List.of(new GazeOfGranite()));
        harness.addMana(player1, ManaColor.BLACK, 2 + xValue);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Destroys nonland permanents with mana value X or less and spares bigger ones")
    void destroysPermanentsWithinManaValueBound() {
        harness.addToBattlefield(player1, new LlanowarElves()); // mana value 1
        harness.addToBattlefield(player2, new HillGiant()); // mana value 4
        harness.addToBattlefield(player2, new GrizzlyBears()); // mana value 2

        castGaze(2);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Spares lands regardless of their mana value")
    void sparesLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGaze(3);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys noncreature permanents such as artifacts")
    void destroysNoncreaturePermanents() {
        harness.addToBattlefield(player2, new SerraAngel()); // mana value 5, survives
        harness.addToBattlefield(player2, new HowlingMine()); // mana value 2

        castGaze(2);

        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("X=0 destroys only mana value 0 permanents")
    void xZeroDestroysOnlyZeroCostPermanents() {
        harness.addToBattlefield(player2, new LlanowarElves()); // mana value 1

        castGaze(0);

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("X=0 destroys zero-cost nonlands on both battlefields but spares lands")
    void xZeroDestroysZeroCostNonlands() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Forest());

        castGaze(0);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Destroys enchantments at the X boundary")
    void destroysEnchantmentsAtBoundary() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());

        castGaze(3);

        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Hexproof does not prevent the untargeted destruction")
    void destroysHexproofCreature() {
        harness.addToBattlefield(player2, new TrollAscetic());

        castGaze(3);

        harness.assertInGraveyard(player2, "Troll Ascetic");
    }

    @Test
    @DisplayName("Regeneration saves a matching creature while other permanents are destroyed")
    void allowsRegeneration() {
        harness.addToBattlefield(player1, new TrollAscetic());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        castGaze(3);

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        harness.assertNotInGraveyard(player1, "Troll Ascetic");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X=0 destroys a face-down creature regardless of its face-up mana cost")
    void destroysFaceDownCreatureAtZero() {
        harness.setHand(player1, List.of(new Willbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        castGaze(0);

        harness.assertInGraveyard(player1, "Willbender");
        harness.assertNotOnBattlefield(player1, "Willbender");
    }

    @Test
    @DisplayName("X=0 destroys a creature token even when its power and toughness are large")
    void destroysWurmTokenAtZero() {
        harness.setHand(player1, List.of(new AdventOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.assertOnBattlefield(player1, "Wurm");

        castGaze(0);

        harness.assertNotOnBattlefield(player1, "Wurm");
    }
}
