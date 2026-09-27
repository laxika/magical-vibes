package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossusSteelStalwart.class, GrizzlyBears.class})
class ColossusSteelStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Has indestructible during its controller's turn only")
    void hasIndestructibleDuringItsControllersTurnOnly() {
        Permanent colossus = addCreatureReady(player1, new ColossusSteelStalwart());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Gives other Mutants you control +1/+1")
    void buffsOtherMutantsYouControl() {
        Permanent colossus = addCreatureReady(player1, new ColossusSteelStalwart());
        Permanent otherMutant = addCreatureReady(player1, new ColossusSteelStalwart());
        Permanent opponentMutant = addCreatureReady(player2, new ColossusSteelStalwart());
        Permanent nonMutant = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherMutant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherMutant)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentMutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentMutant)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonMutant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonMutant)).isEqualTo(2);
    }
}
