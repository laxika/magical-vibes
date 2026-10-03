package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({DragonskullSummit.class, Forest.class, Mountain.class, Swamp.class})
class DragonskullSummitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Forest)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Swamp and a Mountain")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Swamp does not satisfy the check")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new DragonskullSummit());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        harness.addToBattlefield(player1, new DragonskullSummit());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Swamp still allows the Summit to enter untapped")
    void tappedSwampStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Swamp()).tap();

        Permanent summit = harness.enterBattlefieldAndReturn(player1, new DragonskullSummit());

        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Mountain still allows the Summit to enter untapped")
    void tappedMountainStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();

        Permanent summit = harness.enterBattlefieldAndReturn(player1, new DragonskullSummit());

        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Mountain does not satisfy the check")
    void opponentMountainDoesNotCount() {
        harness.addToBattlefield(player2, new Mountain());

        Permanent summit = harness.enterBattlefieldAndReturn(player1, new DragonskullSummit());

        assertThat(summit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Summit does not count as a Swamp or Mountain")
    void anotherSummitDoesNotQualify() {
        harness.addToBattlefield(player1, new DragonskullSummit());

        Permanent summit = harness.enterBattlefieldAndReturn(player1, new DragonskullSummit());

        assertThat(summit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Summit can produce mana on the turn it enters")
    void canProduceManaImmediatelyAfterEntering() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findSummit(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findSummit(Player player) {
        return findPermanent(player, "Dragonskull Summit");
    }
}
