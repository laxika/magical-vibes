package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorosMastiff;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosShredFreak;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazeGlider.class, GrizzlyBears.class, RakdosShredFreak.class, BorosMastiff.class})
class MazeGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to a multicolored creature you control, and revokes it when the Glider leaves")
    void grantsFlyingToOwnMulticoloredCreature() {
        Permanent glider = addCreatureReady(player1, new MazeGlider());
        Permanent gold = addCreatureReady(player1, new RakdosShredFreak());

        assertThat(gqs.hasKeyword(gd, gold, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(glider);

        assertThat(gqs.hasKeyword(gd, gold, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flying to a monocolored creature you control")
    void doesNotGrantToMonocoloredCreature() {
        addCreatureReady(player1, new MazeGlider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flying to an opponent's multicolored creature")
    void doesNotGrantToOpponentMulticoloredCreature() {
        addCreatureReady(player1, new MazeGlider());
        Permanent opponentGold = addCreatureReady(player2, new RakdosShredFreak());

        assertThat(gqs.hasKeyword(gd, opponentGold, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Updates flying when a creature becomes multicolored or loses its additional color")
    void updatesFlyingWhenColorsChange() {
        addCreatureReady(player1, new MazeGlider());
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());

        assertThat(gqs.hasKeyword(gd, mastiff, Keyword.FLYING)).isFalse();

        harness.inMutationScope(() -> mastiff.getGrantedColors().add(CardColor.BLUE));
        assertThat(gqs.hasKeyword(gd, mastiff, Keyword.FLYING)).isTrue();

        harness.inMutationScope(() -> mastiff.getGrantedColors().clear());
        assertThat(gqs.hasKeyword(gd, mastiff, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Grants flying to a multicolored creature that enters after the Glider")
    void grantsFlyingToCreatureEnteringLater() {
        addCreatureReady(player1, new MazeGlider());
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        harness.inMutationScope(() -> mastiff.getGrantedColors().add(CardColor.BLUE));
        assertThat(gqs.hasKeyword(gd, mastiff, Keyword.FLYING)).isTrue();

        Permanent newcomer = addCreatureReady(player1, new BorosMastiff());
        harness.inMutationScope(() -> newcomer.getGrantedColors().add(CardColor.GREEN));
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying follows the Glider's current controller")
    void updatesFlyingWhenGliderChangesController() {
        Permanent glider = addCreatureReady(player1, new MazeGlider());
        Permanent own = addCreatureReady(player1, new BorosMastiff());
        Permanent opposing = addCreatureReady(player2, new BorosMastiff());
        harness.inMutationScope(() -> {
            own.getGrantedColors().add(CardColor.BLUE);
            opposing.getGrantedColors().add(CardColor.BLUE);
        });

        assertThat(gqs.hasKeyword(gd, own, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FLYING)).isFalse();

        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(glider);
            gd.playerBattlefields.get(player2.getId()).add(glider);
        });

        assertThat(gqs.hasKeyword(gd, own, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FLYING)).isTrue();
    }
}
