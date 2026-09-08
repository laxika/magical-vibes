package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpelleaterWolverine.class, Shock.class, Divination.class})
class SpelleaterWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike with fewer than three instant or sorcery cards in its controller's graveyard")
    void noDoubleStrikeBelowThreshold() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with three instant or sorcery cards in its controller's graveyard")
    void gainsDoubleStrikeAtThreshold() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature cards and cards in an opponent's graveyard do not count")
    void onlyControllerInstantAndSorceryCardsCount() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new SpelleaterWolverine()));
        harness.setGraveyard(player2, List.of(new Shock()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when its controller's graveyard drops below the threshold")
    void losesDoubleStrikeWhenGraveyardShrinks() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new Shock()));
        Permanent wolverine = addWolverine();
        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addWolverine() {
        return harness.addToBattlefieldAndReturn(player1, new SpelleaterWolverine());
    }
}
