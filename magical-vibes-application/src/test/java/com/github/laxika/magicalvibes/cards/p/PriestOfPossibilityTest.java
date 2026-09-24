package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.y.YavimayaScion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfPossibility.class, SerraAngel.class, AvatarOfMight.class,
        GrizzlyBears.class, YavimayaScion.class, NuisanceEngine.class})
class PriestOfPossibilityTest extends BaseCardTest {

    @Test
    void gainsKeywordsAndProtectionFromTheTopSeven() {
        harness.setLibrary(player1, List.of(
                new SerraAngel(),
                new AvatarOfMight(),
                new YavimayaScion(),
                new GrizzlyBears()));

        Permanent priest = harness.enterBattlefieldAndReturn(player1, new PriestOfPossibility());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, priest, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.TRAMPLE)).isTrue();

        Permanent artifact = new Permanent(new NuisanceEngine());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, priest, artifact)).isTrue();
    }

    @Test
    void onlyChecksTheTopSevenCards() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new SerraAngel()));

        Permanent priest = harness.enterBattlefieldAndReturn(player1, new PriestOfPossibility());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, priest, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, priest, Keyword.VIGILANCE)).isFalse();
    }
}
