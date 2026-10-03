package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({DarkslickShores.class, Mountain.class, Memnite.class})
class DarkslickShoresTest extends BaseCardTest {

    @Test
    @DisplayName("Enters untapped when you control zero other lands")
    void entersUntappedWithZeroLands() {
        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control one other land")
    void entersUntappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters tapped when you control three other lands")
    void entersTappedWithThreeLands() {
        addBasicLand(player1);
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control many other lands")
    void entersTappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        // Add 3 creatures (not lands)
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }

        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        // 0 lands, 3 creatures — should enter untapped
        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        // Give opponent 5 lands
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        // Player1 has 0 other lands — should enter untapped
        Permanent shores = findShores(player1);
        assertThat(shores.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new DarkslickShores());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new DarkslickShores());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly played untapped Shores can produce mana immediately")
    void newlyPlayedLandProducesManaImmediately() {
        harness.setHand(player1, List.of(new DarkslickShores()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findShores(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry outside a land play still checks the controller's other lands")
    void enteringWithoutPlayingChecksOtherLands() {
        for (int i = 0; i < 3; i++) {
            addBasicLand(player1);
        }

        Permanent shores = harness.enterBattlefieldAndReturn(player1, new DarkslickShores());

        assertThat(shores.isTapped()).isTrue();
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findShores(Player player) {
        return findPermanent(player, "Darkslick Shores");
    }
}
