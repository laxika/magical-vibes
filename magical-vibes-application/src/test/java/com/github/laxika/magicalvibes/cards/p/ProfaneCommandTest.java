package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfaneCommandTest extends BaseCardTest {

    // Mode indices: 0 = target player loses X life,
    //               1 = return creature card MV≤X from GY to battlefield,
    //               2 = target creature gets -X/-X until end of turn,
    //               3 = up to X target creatures gain fear until end of turn.

    @Test
    @DisplayName("Lose-life + -X/-X: burns a player and shrinks a creature")
    void loseLifeAndShrinkCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 2}, 3,
                List.of(player2.getId(), bearsId));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears"); // 2/2 with -3/-3 dies
    }

    @Test
    @DisplayName("Reanimate + fear: returns a MV≤X creature and grants fear to up to X creatures")
    void reanimateAndGrantFear() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        UUID gyBearsId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new HillGiant());
        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 3}, 2, gyBearsId,
                List.of(angelId, giantId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent angel = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(angelId)).findFirst().orElseThrow();
        Permanent giant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(giantId)).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 1,
                List.of(player2.getId(), bearsId));
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(bearsId)).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Reanimate rejects a creature card with mana value greater than X")
    void reanimateRejectsManaValueAboveX() {
        harness.setGraveyard(player1, List.of(new SerraAngel())); // MV 5
        UUID gyAngelId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 2}, 2, gyAngelId,
                        List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fear mode with X=0 chooses no creatures and still resolves the other mode")
    void fearModeWithXZeroChoosesNoCreatures() {
        harness.setHand(player1, List.of(new ProfaneCommand()));
        harness.addMana(player1, ManaColor.BLACK, 2); // X=0 + {B}{B}

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 0,
                List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
