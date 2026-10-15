package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
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

@CardUsed({ShipwreckMarsh.class, NoviceOccultist.class})
class ShipwreckMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control zero other lands")
    void entersTappedWithZeroLands() {
        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control one other land")
    void entersTappedWithOneLand() {
        addLand(player1);

        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addLand(player1);
        addLand(player1);

        playMarsh();

        assertThat(findMarsh().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new NoviceOccultist());
        }

        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addLand(player2);
        }

        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new ShipwreckMarsh());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new ShipwreckMarsh());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped lands count toward entering untapped")
    void tappedLandsCount() {
        harness.addToBattlefieldAndReturn(player1, new ShipwreckMarsh()).tap();
        harness.addToBattlefieldAndReturn(player1, new ShipwreckMarsh()).tap();

        playMarsh();

        assertThat(findMarsh().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped with more than two other lands")
    void entersUntappedWithThreeLands() {
        for (int i = 0; i < 3; i++) {
            addLand(player1);
        }

        playMarsh();

        assertThat(findMarsh().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can produce mana immediately after entering untapped")
    void producesManaOnEntryTurn() {
        addLand(player1);
        addLand(player1);
        playMarsh();

        harness.activateAbility(player1, 2, 0, null, null);

        assertThat(findMarsh().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playMarsh() {
        harness.setHand(player1, List.of(new ShipwreckMarsh()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
    }

    private void addLand(Player player) {
        harness.addToBattlefield(player, new ShipwreckMarsh());
    }

    private Permanent findMarsh() {
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
