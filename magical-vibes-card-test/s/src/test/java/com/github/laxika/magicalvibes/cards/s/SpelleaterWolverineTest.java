package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.f.FireProphecy;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpelleaterWolverine.class, FireProphecy.class, CatharticReunion.class})
class SpelleaterWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike with fewer than three instant or sorcery cards in its controller's graveyard")
    void noDoubleStrikeBelowThreshold() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with three instant or sorcery cards in its controller's graveyard")
    void gainsDoubleStrikeAtThreshold() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion(), new FireProphecy()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature cards and cards in an opponent's graveyard do not count")
    void onlyControllerInstantAndSorceryCardsCount() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion(), new SpelleaterWolverine()));
        harness.setGraveyard(player2, List.of(new FireProphecy()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses double strike when its controller's graveyard drops below the threshold")
    void losesDoubleStrikeWhenGraveyardShrinks() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion(), new FireProphecy()));
        Permanent wolverine = addWolverine();
        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion()));

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent addWolverine() {
        return harness.addToBattlefieldAndReturn(player1, new SpelleaterWolverine());
    }

    @Test
    @DisplayName("Three instant cards alone satisfy the threshold")
    void gainsDoubleStrikeWithOnlyInstants() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new FireProphecy(), new FireProphecy()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Three sorcery cards alone satisfy the threshold")
    void gainsDoubleStrikeWithOnlySorceries() {
        harness.setGraveyard(player1, List.of(new CatharticReunion(), new CatharticReunion(), new CatharticReunion()));
        Permanent wolverine = addWolverine();

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Gains double strike immediately when the graveyard reaches the threshold")
    void gainsDoubleStrikeWhenGraveyardGrows() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion()));
        Permanent wolverine = addWolverine();
        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion(), new FireProphecy(), new CatharticReunion()));

        assertThat(gqs.hasKeyword(gd, wolverine, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Each Wolverine checks its own controller's graveyard and grants double strike only to itself")
    void eachControllerUsesTheirOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new FireProphecy(), new CatharticReunion(), new FireProphecy()));
        Permanent first = addWolverine();
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SpelleaterWolverine());

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new CatharticReunion(), new CatharticReunion(), new CatharticReunion()));

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
