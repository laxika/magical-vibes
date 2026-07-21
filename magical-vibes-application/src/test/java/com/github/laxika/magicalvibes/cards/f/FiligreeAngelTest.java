package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Filigree Angel")
class FiligreeAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life per artifact you control, counting the Angel itself")
    void gainsPerArtifactIncludingSelf() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castAndResolveEtb();

        // 2 Ornithopters + the Angel itself (an artifact creature) = 3 artifacts * 3 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 9);
    }

    @Test
    @DisplayName("With no other artifacts, gains 3 life for the Angel alone")
    void gainsForSelfOnly() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castAndResolveEtb();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Artifacts an opponent controls are not counted")
    void ignoresOpponentArtifacts() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castAndResolveEtb();

        // Only the Angel itself counts for player1.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FiligreeAngel()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → gain life
    }
}
