package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GlacialFortress.class, Plains.class, Island.class, Forest.class})
class GlacialFortressTest extends BaseCardTest {

    @Test
    void entersTappedWithoutQualifyingLand() {
        assertThat(playFortress().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTappedWithForest() {
        harness.addToBattlefield(player1, new Forest());
        assertThat(playFortress().isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        assertThat(playFortress().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());
        assertThat(playFortress().isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithBothQualifyingTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        assertThat(playFortress().isTapped()).isFalse();
    }

    @Test
    void tappedPlainsStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        assertThat(playFortress().isTapped()).isFalse();
    }

    @Test
    void tappedIslandStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        assertThat(playFortress().isTapped()).isFalse();
    }

    @Test
    void opponentsQualifyingLandsDoNotCount() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        assertThat(playFortress().isTapped()).isTrue();
    }

    @Test
    void anotherFortressDoesNotQualify() {
        harness.addToBattlefield(player1, new GlacialFortress());
        assertThat(playFortress().isTapped()).isTrue();
    }

    @Test
    void enteringWithoutLandPlayStillAppliesReplacement() {
        Permanent fortress = harness.enterBattlefieldAndReturn(player1, new GlacialFortress());
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    void enteringWithoutLandPlayWithPlainsIsUntapped() {
        harness.addToBattlefield(player1, new Plains());
        Permanent fortress = harness.enterBattlefieldAndReturn(player1, new GlacialFortress());
        assertThat(fortress.isTapped()).isFalse();
    }

    @Test
    void producesWhiteManaImmediatelyOnEntering() {
        harness.addToBattlefield(player1, new Plains());
        Permanent fortress = playFortress();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(fortress.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesBlueManaImmediatelyOnEntering() {
        harness.addToBattlefield(player1, new Island());
        Permanent fortress = playFortress();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(fortress.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotProduceManaWhileTapped() {
        playFortress();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent playFortress() {
        harness.setHand(player1, List.of(new GlacialFortress()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
