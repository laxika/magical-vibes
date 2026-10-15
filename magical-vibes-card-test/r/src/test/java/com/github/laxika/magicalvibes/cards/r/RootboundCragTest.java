package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootboundCrag.class, Forest.class, Island.class, Mountain.class})
class RootboundCragTest extends BaseCardTest {

    @Test
    void entersTappedWithoutQualifyingLand() {
        Permanent crag = playCrag();

        assertThat(crag.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void entersTappedWithOnlyIsland() {
        harness.addToBattlefield(player1, new Island());

        assertThat(playCrag().isTapped()).isTrue();
    }

    @Test
    void anotherCragDoesNotQualify() {
        harness.addToBattlefield(player1, new RootboundCrag());

        assertThat(playCrag().isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        assertThat(playCrag().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithForest() {
        harness.addToBattlefield(player1, new Forest());

        assertThat(playCrag().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithBothTypes() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(playCrag().isTapped()).isFalse();
    }

    @Test
    void tappedMountainStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();

        assertThat(harness.enterBattlefieldAndReturn(player1, new RootboundCrag()).isTapped()).isFalse();
    }

    @Test
    void tappedForestStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        assertThat(harness.enterBattlefieldAndReturn(player1, new RootboundCrag()).isTapped()).isFalse();
    }

    @Test
    void opponentsLandsDoNotQualify() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        assertThat(playCrag().isTapped()).isTrue();
    }

    @Test
    void producesRedImmediatelyWithoutUsingStack() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent crag = playCrag();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesGreenImmediatelyWithoutUsingStack() {
        harness.addToBattlefield(player1, new Forest());
        Permanent crag = playCrag();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent playCrag() {
        harness.setHand(player1, List.of(new RootboundCrag()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        return findPermanents(player1, "Rootbound Crag").getLast();
    }
}
