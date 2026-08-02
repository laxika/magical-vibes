package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DanceOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0 and gain fear")
    void boostsAndGrantsFearToOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(3);
            assertThat(p.getEffectiveToughness()).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, p, Keyword.FEAR)).isTrue();
        }
        harness.assertInGraveyard(player1, "Dance of Shadows");
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent theirs = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Boost and fear wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Resolves with an empty battlefield")
    void resolvesWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new DanceOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
