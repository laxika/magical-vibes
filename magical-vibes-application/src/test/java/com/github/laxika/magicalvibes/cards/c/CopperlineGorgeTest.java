package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CopperlineGorge.class, Memnite.class, Mountain.class})
class CopperlineGorgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters untapped when you control zero other lands")
    void entersUntappedWithZeroLands() {
        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control one other land")
    void entersUntappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters tapped when you control three other lands")
    void entersTappedWithThreeLands() {
        addBasicLand(player1);
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control many other lands")
    void entersTappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }

        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent gorge = findGorge(player1);
        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addGorgeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addGorgeReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped without a land play when you control two other lands")
    void entersUntappedWithoutLandPlayWithTwoOtherLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        Permanent gorge = harness.enterBattlefieldAndReturn(player1, new CopperlineGorge());

        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other tapped nonbasic lands count toward the entry check")
    void tappedNonbasicLandsCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new CopperlineGorge()).tap();
        }

        Permanent gorge = harness.enterBattlefieldAndReturn(player1, new CopperlineGorge());

        assertThat(gorge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two other lands plus creatures and opposing lands still allow untapped entry")
    void unrelatedPermanentsDoNotChangeTwoLandBoundary() {
        addBasicLand(player1);
        addBasicLand(player1);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
            addBasicLand(player2);
        }

        Permanent gorge = harness.enterBattlefieldAndReturn(player1, new CopperlineGorge());

        assertThat(gorge.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters tapped without a land play when you control three other lands")
    void entersTappedWithoutLandPlayWithThreeOtherLands() {
        addBasicLand(player1);
        addBasicLand(player1);
        addBasicLand(player1);

        Permanent gorge = harness.enterBattlefieldAndReturn(player1, new CopperlineGorge());

        assertThat(gorge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Gorge can produce mana immediately after entering")
    void canProduceManaImmediatelyAfterEntering() {
        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findGorge(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGorgeReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CopperlineGorge());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addBasicLand(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findGorge(Player player) {
        return findPermanent(player, "Copperline Gorge");
    }
}
