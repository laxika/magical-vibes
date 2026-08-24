package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarythTheVipersFang.class, GrizzlyBears.class, Forest.class})
class SarythTheVipersFangTest extends BaseCardTest {

    @Test
    void tappedAndUntappedCreaturesGetTheAppropriateKeyword() {
        addSaryth();
        Permanent tappedBear = addTappedBear(player1);
        Permanent untappedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tappedForest = addTappedForest();

        assertThat(gqs.hasKeyword(gd, tappedBear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, tappedBear, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, untappedBear, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, untappedBear, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, tappedForest, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void keywordGrantTracksTapState() {
        addSaryth();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HEXPROOF)).isTrue();

        bear.tap();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HEXPROOF)).isFalse();

        bear.untap();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void abilityUntapsAnotherCreatureYouControl() {
        addSaryth();
        Permanent bear = addTappedBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void abilityUntapsAnotherLandYouControl() {
        addSaryth();
        Permanent forest = addTappedForest();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void abilityCannotTargetSourceOrAnOpponentPermanent() {
        Permanent saryth = addSaryth();
        Permanent opponentBear = addTappedBear(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, saryth.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSaryth() {
        Permanent saryth = harness.addToBattlefieldAndReturn(player1, new SarythTheVipersFang());
        saryth.setSummoningSick(false);
        return saryth;
    }

    private Permanent addTappedBear(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.tap();
        return bear;
    }

    private Permanent addTappedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        return forest;
    }
}
