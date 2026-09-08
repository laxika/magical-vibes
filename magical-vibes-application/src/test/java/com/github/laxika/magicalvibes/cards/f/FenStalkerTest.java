package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FenStalkerTest extends BaseCardTest {

    @Test
    void hasFearWhenYouControlNoUntappedLands() {
        Permanent fenStalker = addFenStalker();

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void losesFearWhileYouControlAnUntappedLand() {
        Permanent fenStalker = addFenStalker();
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isFalse();
    }

    @Test
    void regainsFearWhenAllYourLandsAreTapped() {
        Permanent fenStalker = addFenStalker();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
    }

    private Permanent addFenStalker() {
        return harness.addToBattlefieldAndReturn(player1, new FenStalker());
    }
}
