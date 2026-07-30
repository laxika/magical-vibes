package com.github.laxika.magicalvibes.cards.h;

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

class HideousVisageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Hideous Visage gives intimidate to each creature you control")
    void grantsIntimidateToOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        for (Permanent own : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.hasKeyword(gd, own, Keyword.INTIMIDATE)).isTrue();
        }
        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Intimidate wears off at end of turn")
    void intimidateWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain intimidate")
    void laterCreaturesDoNotGainIntimidate() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HideousVisage()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new GrizzlyBears());

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(gqs.hasKeyword(gd, battlefield.getFirst(), Keyword.INTIMIDATE)).isTrue();
        assertThat(gqs.hasKeyword(gd, battlefield.getLast(), Keyword.INTIMIDATE)).isFalse();
    }
}
