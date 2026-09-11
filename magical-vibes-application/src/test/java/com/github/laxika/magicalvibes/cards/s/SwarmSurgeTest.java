package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmSurge.class, GrizzlyBears.class, Ornithopter.class})
class SwarmSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures, and gives first strike to your colorless creatures")
    void boostsOwnCreaturesAndGrantsFirstStrikeToColorlessCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bonuses wear off at end of turn")
    void bonusesWearOffAtEndOfTurn() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        cast();
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isZero();
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new SwarmSurge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
