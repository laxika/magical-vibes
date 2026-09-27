package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Drumbellower.class, Forest.class, GrizzlyBears.class, Humility.class})
class DrumbellowerTest extends BaseCardTest {

    @Test
    @DisplayName("Drumbellower untaps creatures you control during an opponent's untap step")
    void untapsYourCreaturesOnOpponentsUntapStep() {
        Permanent drumbellower = addCreatureReady(player1, new Drumbellower());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        drumbellower.tap();
        bears.tap();
        forest.tap();

        harness.performUntapStep(player2);

        assertThat(drumbellower.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without Drumbellower, non-active player's tapped creatures stay tapped")
    void withoutDrumbellowerCreaturesStayTapped() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Drumbellower only untaps creatures its controller controls")
    void onlyControllerCreaturesUntap() {
        Permanent drumbellower = addCreatureReady(player1, new Drumbellower());
        Permanent p1Bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent p2Bears = addCreatureReady(player2, new GrizzlyBears());

        drumbellower.tap();
        p1Bears.tap();
        p2Bears.tap();

        harness.performUntapStep(player1);

        assertThat(drumbellower.isTapped()).isFalse();
        assertThat(p1Bears.isTapped()).isFalse();
        assertThat(p2Bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Drumbellower does not untap creatures after losing all abilities")
    void doesNotUntapCreaturesWhenItHasLostAllAbilities() {
        Permanent drumbellower = addCreatureReady(player1, new Drumbellower());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Humility());

        drumbellower.tap();
        bears.tap();
        assertThat(gqs.hasLostAllAbilities(gd, drumbellower)).isTrue();

        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }
}
