package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinehornMinotaur.class, GrizzlyBears.class})
class SpinehornMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Gains double strike after its controller draws two cards")
    void gainsDoubleStrikeAfterControllerDrawsTwoCards() {
        Permanent minotaur = addCreatureReady(player1, new SpinehornMinotaur());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DOUBLE_STRIKE)).isFalse();

        draw(player1);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DOUBLE_STRIKE)).isFalse();

        draw(player1);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not grant double strike")
    void opponentDrawsDoNotGrantDoubleStrike() {
        Permanent minotaur = addCreatureReady(player1, new SpinehornMinotaur());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
