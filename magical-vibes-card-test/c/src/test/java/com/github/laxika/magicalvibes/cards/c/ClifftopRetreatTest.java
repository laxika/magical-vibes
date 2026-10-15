package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({ClifftopRetreat.class, Mountain.class, Plains.class, Swamp.class})
class ClifftopRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent retreat = findRetreat(player1);
        assertThat(retreat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Swamp)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent retreat = findRetreat(player1);
        assertThat(retreat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent retreat = findRetreat(player1);
        assertThat(retreat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());

        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent retreat = findRetreat(player1);
        assertThat(retreat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Mountain and a Plains")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());

        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent retreat = findRetreat(player1);
        assertThat(retreat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Mountain does not satisfy the check")
    void opponentMountainDoesNotCount() {
        harness.addToBattlefield(player2, new Mountain());

        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent retreat = findRetreat(player1);
        assertThat(retreat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addRetreatReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addRetreatReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }


    @Test
    @DisplayName("A tapped Mountain still allows Clifftop Retreat to enter untapped")
    void tappedMountainQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findRetreat(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Clifftop Retreat does not satisfy the entry condition")
    void anotherRetreatDoesNotQualify() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ClifftopRetreat());
        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(existing.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Opponent's Plains does not satisfy the entry condition")
    void opponentPlainsDoesNotCount() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new ClifftopRetreat()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findRetreat(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without a land play still applies the tapped replacement")
    void entersTappedWhenPutOntoBattlefield() {
        Permanent retreat = harness.enterBattlefieldAndReturn(player1, new ClifftopRetreat());

        assertThat(retreat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without a land play checks the new controller's Plains")
    void entersUntappedWhenPutOntoBattlefieldWithPlains() {
        harness.addToBattlefield(player2, new Plains());

        Permanent retreat = harness.enterBattlefieldAndReturn(player2, new ClifftopRetreat());

        assertThat(retreat.isTapped()).isFalse();
    }

    private Permanent addRetreatReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ClifftopRetreat());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent findRetreat(Player player) {
        return findPermanent(player, "Clifftop Retreat");
    }
}
