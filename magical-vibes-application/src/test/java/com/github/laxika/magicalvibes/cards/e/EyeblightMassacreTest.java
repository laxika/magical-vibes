package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeblightMassacre.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class EyeblightMassacreTest extends BaseCardTest {

    @Test
    @DisplayName("Gives non-Elf creatures on both sides -2/-2 and leaves Elves untouched")
    void weakensOnlyNonElves() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyElf = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EyeblightMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, enemyElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyElf)).isEqualTo(1);
        // The 2/2 Bears drops to 0/0 and is put into the graveyard by state-based actions.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new EyeblightMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Weakens non-Elves and spares Elves controlled by either player")
    void affectsBothPlayersNonElves() {
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        Permanent enemyGiant = addCreatureReady(player2, new HillGiant());
        Permanent ownElf = addCreatureReady(player1, new LlanowarElves());
        Permanent enemyElf = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EyeblightMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        for (Permanent giant : List.of(ownGiant, enemyGiant)) {
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        }
        for (Permanent elf : List.of(ownElf, enemyElf)) {
            assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        }
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void doesNotAffectCreaturesEnteringLater() {
        harness.setHand(player1, List.of(new EyeblightMassacre(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
