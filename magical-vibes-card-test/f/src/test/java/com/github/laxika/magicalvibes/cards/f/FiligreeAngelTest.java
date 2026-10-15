package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Filigree Angel")
@CardUsed({FiligreeAngel.class, Ornithopter.class, FieldmistBorderpost.class})
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

    @Test
    @DisplayName("Counts noncreature artifacts added before the trigger resolves")
    void countsArtifactsAtResolution() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castAngel();
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore);

        harness.addToBattlefield(player1, new FieldmistBorderpost());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 6);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger still gains life from remaining artifacts after the Angel leaves")
    void triggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new FieldmistBorderpost());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castAngel();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof FiligreeAngel);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains no life when no artifacts remain at resolution")
    void gainsNothingWhenSourceLeavesAndNoArtifactsRemain() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castAngel();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void castAngel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FiligreeAngel(), "{5}{W}{W}{U}");
    }

    private void castAndResolveEtb() {
        castAngel();
        harness.passBothPriorities(); // resolve creature spell â†’ ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger â†’ gain life
    }
}
