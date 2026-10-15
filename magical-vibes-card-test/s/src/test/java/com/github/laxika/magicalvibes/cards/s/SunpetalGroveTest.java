package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunpetalGrove.class, Plains.class, Forest.class, Island.class})
class SunpetalGroveTest extends BaseCardTest {

    @Test
    void entersTappedWithoutQualifyingLand() {
        assertThat(playGrove().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTappedWithIsland() {
        harness.addToBattlefield(player1, new Island());
        assertThat(playGrove().isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        assertThat(playGrove().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithForest() {
        harness.addToBattlefield(player1, new Forest());
        assertThat(playGrove().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithBothQualifyingTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Forest());
        assertThat(playGrove().isTapped()).isFalse();
    }

    @Test
    void tappedPlainsStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        assertThat(playGrove().isTapped()).isFalse();
    }

    @Test
    void tappedForestStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        assertThat(playGrove().isTapped()).isFalse();
    }

    @Test
    void opponentsQualifyingLandsDoNotCount() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Forest());
        assertThat(playGrove().isTapped()).isTrue();
    }

    @Test
    void anotherGroveDoesNotQualify() {
        harness.addToBattlefield(player1, new SunpetalGrove());
        assertThat(playGrove().isTapped()).isTrue();
    }

    @Test
    void enteringWithoutLandPlayStillAppliesReplacement() {
        Permanent grove = harness.enterBattlefieldAndReturn(player1, new SunpetalGrove());
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    void enteringWithoutLandPlayWithPlainsIsUntapped() {
        harness.addToBattlefield(player1, new Plains());
        Permanent grove = harness.enterBattlefieldAndReturn(player1, new SunpetalGrove());
        assertThat(grove.isTapped()).isFalse();
    }

    @Test
    void producesWhiteManaImmediatelyOnEntering() {
        harness.addToBattlefield(player1, new Plains());
        Permanent grove = playGrove();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesGreenManaImmediatelyOnEntering() {
        harness.addToBattlefield(player1, new Forest());
        Permanent grove = playGrove();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotProduceManaWhileTapped() {
        playGrove();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent playGrove() {
        harness.setHand(player1, List.of(new SunpetalGrove()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
