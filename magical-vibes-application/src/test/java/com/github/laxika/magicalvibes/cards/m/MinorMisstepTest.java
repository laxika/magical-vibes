package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.c.ChimericMass;
import com.github.laxika.magicalvibes.cards.e.EvolvingAdaptive;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinorMisstep.class, EvolvingAdaptive.class, CopperLonglegs.class, Memnite.class, ChimericMass.class})
class MinorMisstepTest extends BaseCardTest {

    @Test
    void countersXSpellWhenChosenXIsOne() {
        ChimericMass mass = new ChimericMass();
        harness.setHand(player1, List.of(mass));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mass.getId());

        harness.assertInGraveyard(player1, "Chimeric Mass");
        harness.assertInGraveyard(player2, "Minor Misstep");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void cannotTargetXSpellWhenChosenXIsTwo() {
        ChimericMass mass = new ChimericMass();
        harness.setHand(player1, List.of(mass));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, mass.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersSpellWithManaValueZero() {
        Memnite memnite = new Memnite();
        harness.setHand(player1, List.of(memnite));
        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, memnite.getId());

        harness.assertInGraveyard(player1, "Memnite");
        harness.assertInGraveyard(player2, "Minor Misstep");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void canCounterAnotherMinorMisstep() {
        EvolvingAdaptive adaptive = new EvolvingAdaptive();
        MinorMisstep opposingMisstep = new MinorMisstep();
        harness.setHand(player1, List.of(adaptive, new MinorMisstep()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(opposingMisstep));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, adaptive.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, opposingMisstep.getId());

        harness.assertInGraveyard(player1, "Minor Misstep");
        harness.assertInGraveyard(player2, "Minor Misstep");
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Evolving Adaptive");
    }

    @Test
    void canTargetSpellWithManaValueOne() {
        EvolvingAdaptive elves = new EvolvingAdaptive();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        assertThat(harness.getGameData().stack).hasSize(2);
    }

    @Test
    void cannotTargetSpellWithManaValueTwo() {
        CopperLonglegs bears = new CopperLonglegs();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersSpellWithManaValueOne() {
        EvolvingAdaptive elves = new EvolvingAdaptive();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player1, "Evolving Adaptive");
        harness.assertInGraveyard(player2, "Minor Misstep");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void fizzlesIfTargetSpellLeavesTheStack() {
        EvolvingAdaptive elves = new EvolvingAdaptive();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MinorMisstep()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        harness.getGameData().stack.removeIf(entry -> entry.getCard().getName().equals("Evolving Adaptive"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Minor Misstep");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
