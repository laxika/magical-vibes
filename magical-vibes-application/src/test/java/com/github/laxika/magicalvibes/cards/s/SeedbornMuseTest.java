package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedbornMuse.class, Forest.class, GrizzlyBears.class, Humility.class})
class SeedbornMuseTest extends BaseCardTest {

    @Test
    @DisplayName("Seedborn Muse untaps all your permanents during opponent's untap step")
    void untapsAllYourPermanentsOnOpponentsUntapStep() {
        Permanent muse = addCreatureReady(player1, new SeedbornMuse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        muse.tap();
        bears.tap();
        forest.tap();
        assertThat(muse.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(muse.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Without Seedborn Muse, non-active player's tapped permanents stay tapped")
    void withoutSeedbornMusePermanentsStayTapped() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        assertThat(bears.isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Seedborn Muse only untaps permanents its controller controls")
    void onlyControllerPermanentsUntap() {
        Permanent p1Muse = addCreatureReady(player1, new SeedbornMuse());
        Permanent p1Bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent p2Bears = addCreatureReady(player2, new GrizzlyBears());

        p1Muse.tap();
        p1Bears.tap();
        p2Bears.tap();

        harness.performUntapStep(player1); // player1's own untap step

        assertThat(p1Muse.isTapped()).isFalse();
        assertThat(p1Bears.isTapped()).isFalse();
        assertThat(p2Bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Seedborn Muse does not untap permanents after losing all abilities")
    void doesNotUntapPermanentsWhenItHasLostAllAbilities() {
        Permanent muse = addCreatureReady(player1, new SeedbornMuse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Humility());

        bears.tap();
        assertThat(gqs.hasLostAllAbilities(gd, muse)).isTrue();

        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }
}
