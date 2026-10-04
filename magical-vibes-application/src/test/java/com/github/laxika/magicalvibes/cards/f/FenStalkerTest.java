package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChimericIdol;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FenStalker.class, WintermoonMesa.class, SpiketailHatchling.class, ChimericIdol.class})
class FenStalkerTest extends BaseCardTest {

    @Test
    void hasFearWhenYouControlNoUntappedLands() {
        Permanent fenStalker = addFenStalker();

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void losesFearWhileYouControlAnUntappedLand() {
        Permanent fenStalker = addFenStalker();
        harness.addToBattlefield(player1, new WintermoonMesa());

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isFalse();
    }

    @Test
    void regainsFearWhenAllYourLandsAreTapped() {
        Permanent fenStalker = addFenStalker();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        land.tap();

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void ignoresUntappedLandsControlledByAnOpponent() {
        Permanent fenStalker = addFenStalker();
        harness.addToBattlefield(player2, new WintermoonMesa());

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void fearUpdatesAsTheLastUntappedLandTapsAndUntaps() {
        Permanent fenStalker = addFenStalker();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isFalse();
        land.tap();
        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
        land.untap();
        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isFalse();
    }

    @Test
    void oneUntappedLandPreventsFearEvenWithOtherTappedLands() {
        Permanent fenStalker = addFenStalker();
        harness.addToBattlefieldAndReturn(player1, new WintermoonMesa()).tap();
        Permanent lastLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());

        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isFalse();
        lastLand.tap();
        assertThat(gqs.hasKeyword(gd, fenStalker, Keyword.FEAR)).isTrue();
    }

    @Test
    void doesNotGrantFearToOtherCreatures() {
        addFenStalker();
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SpiketailHatchling());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FEAR)).isFalse();
    }

    @Test
    void nonblackNonartifactCreatureCannotBlockWhileFearIsActive() {
        addCreatureReady(player1, new FenStalker());
        harness.addToBattlefield(player2, new SpiketailHatchling());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    void nonblackCreatureCanBlockWhenAnUntappedLandDisablesFear() {
        addCreatureReady(player1, new FenStalker());
        harness.addToBattlefield(player1, new WintermoonMesa());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SpiketailHatchling());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void blackCreatureCanBlockWhileFearIsActive() {
        addCreatureReady(player1, new FenStalker());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FenStalker());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void colorlessArtifactCreatureCanBlockWhileFearIsActive() {
        addCreatureReady(player1, new FenStalker());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ChimericIdol());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addFenStalker() {
        return harness.addToBattlefieldAndReturn(player1, new FenStalker());
    }
}
