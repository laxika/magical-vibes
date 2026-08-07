package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        addBasicLand(player1);

        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        playMarsh();

        assertThat(findMarsh().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));
        }

        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        playMarsh();

        assertThat(findMarsh().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addMarshReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addMarshReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playMarsh() {
        harness.setHand(player1, List.of(new ShipwreckMarsh()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
    }

    private void addMarshReady(Player player) {
        Permanent perm = new Permanent(new ShipwreckMarsh());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void addBasicLand(Player player) {
        Card land = new Mountain();
        gd.playerBattlefields.get(player.getId()).add(new Permanent(land));
    }

    private Permanent findMarsh() {
        return findPermanent(player1, "Shipwreck Marsh");
    }
}
