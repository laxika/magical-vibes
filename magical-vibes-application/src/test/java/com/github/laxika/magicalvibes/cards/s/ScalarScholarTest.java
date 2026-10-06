package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NuclearFallout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScalarScholar.class, NuclearFallout.class})
class ScalarScholarTest extends BaseCardTest {

    @Test
    void perpetuallyReducesOwnedXSpells() {
        harness.setHand(player1, List.of(new ScalarScholar(), new NuclearFallout()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void incrementUsesManaActuallySpentAfterReduction() {
        harness.setHand(player1, List.of(new NuclearFallout()));
        var scholar = harness.enterBattlefieldAndReturn(player1, new ScalarScholar());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(scholar.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void repeatedEntriesAccumulateReductions() {
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.enterBattlefieldAndReturn(player1, new ScalarScholar());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new ScalarScholar());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionFollowsOwnedLibraryCardIntoHand() {
        var fallout = new NuclearFallout();
        harness.setLibrary(player1, List.of(fallout));
        harness.enterBattlefieldAndReturn(player1, new ScalarScholar());
        harness.passBothPriorities();
        gd.playerDecks.get(player1.getId()).remove(fallout);
        harness.setHand(player1, List.of(fallout));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceOpponentsXSpells() {
        harness.setHand(player2, List.of(new NuclearFallout()));
        harness.enterBattlefieldAndReturn(player1, new ScalarScholar());
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player2, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void reductionCannotPayColoredManaWhenXIsZero() {
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.enterBattlefieldAndReturn(player1, new ScalarScholar());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
