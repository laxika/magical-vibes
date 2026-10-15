package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EclipsedSteppe.class, Forest.class, Mountain.class, EvolvingWilds.class})
class EclipsedSteppeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without two basic lands")
    void entersTappedWithoutTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new EvolvingWilds());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control two basic lands")
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's basic lands do not satisfy the check")
    void opponentBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Produces white mana")
    void producesWhiteMana() {
        harness.addToBattlefield(player1, new EclipsedSteppe());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Produces black mana")
    void producesBlackMana() {
        harness.addToBattlefield(player1, new EclipsedSteppe());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playSteppe() {
        harness.setHand(player1, List.of(new EclipsedSteppe()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    @Test
    @DisplayName("Two basic lands with the same name satisfy the check even when tapped")
    void tappedBasicLandsWithSameNameCount() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("More than two basic lands satisfy the check")
    void entersUntappedWithThreeBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        playSteppe();

        assertThat(findSteppe(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A land with basic land types is not a basic land")
    void anotherSteppeDoesNotCountAsBasic() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new EclipsedSteppe());

        Permanent enteringSteppe = harness.enterBattlefieldAndReturn(player1, new EclipsedSteppe());

        assertThat(enteringSteppe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Steppe entering tapped cannot produce either color")
    void cannotProduceManaWhileTapped() {
        playSteppe();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("An untapped Steppe can produce mana immediately and taps to pay the cost")
    void producesManaImmediatelyAfterEnteringUntapped() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        playSteppe();

        harness.activateAbility(player1, 2, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(findSteppe(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent findSteppe(Player player) {
        return findPermanent(player, "Eclipsed Steppe");
    }
}
