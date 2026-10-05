package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurnaceStrider;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.c.CopperlineGorge;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KothFireOfResistance.class, Mountain.class, Forest.class, HillGiant.class,
        FurnaceStrider.class, KorFirewalker.class, BloodMoon.class, CopperlineGorge.class})
class KothFireOfResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("+2 searches for a basic Mountain and puts it into hand")
    void plusTwoSearchesForBasicMountain() {
        Permanent koth = addReadyKoth(player1, 4);
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 deals damage equal to the Mountains controlled")
    void minusThreeDealsDamageEqualToMountains() {
        Permanent koth = addReadyKoth(player1, 4);
        addMountain(player1);
        addMountain(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ultimate creates a landfall damage emblem")
    void ultimateEmblemDealsDamageWhenMountainEnters() {
        addReadyKoth(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The ultimate emblem ignores non-Mountain lands")
    void ultimateEmblemIgnoresNonMountainLands() {
        addReadyKoth(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void plusTwoCanFailToFindEvenWithMountainInLibrary() {
        addReadyKoth(player1, 4);
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).contains(mountain);
    }

    @Test
    void minusThreeCountsMountainsAtResolutionAndIgnoresOpponentsMountains() {
        addReadyKoth(player1, 4);
        addMountain(player1);
        addMountain(player2);
        addMountain(player2);
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceStrider());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        addMountain(player1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void minusThreeWithNoMountainsDealsNoDamage() {
        addReadyKoth(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceStrider());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void emblemCanDamageCreatureWithProtectionFromRed() {
        addReadyKoth(player1, 7);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KorFirewalker());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kor Firewalker");
    }

    @Test
    void emblemTriggersForNonbasicLandEnteringAsMountain() {
        addReadyKoth(player1, 7);
        harness.addToBattlefield(player1, new BloodMoon());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CopperlineGorge()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void emblemIgnoresOpponentsMountain() {
        addReadyKoth(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Mountain()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyKoth(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KothFireOfResistance());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addMountain(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }
}
