package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KavuMauler;
import com.github.laxika.magicalvibes.cards.m.MeddlingMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FireIce.class, KavuMauler.class, MeddlingMage.class})
class FireIceTest extends BaseCardTest {

    @Test
    void fireDealsTwoDamageDividedAmongTwoTargets() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        int lifeBefore = gd.getLife(player2.getId());

        castFire(Map.of(player2.getId(), 1, kavu.getId(), 1),
                List.of(player2.getId(), kavu.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(kavu.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void fireCanDealAllTwoDamageToOneTarget() {
        int lifeBefore = gd.getLife(player2.getId());

        castFire(Map.of(player2.getId(), 2), List.of(player2.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void fireRejectsMoreThanTwoTargets() {
        Permanent firstKavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        Permanent secondKavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0,
                List.of(player2.getId(), firstKavu.getId(), secondKavu.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fireUsesItsRedModeCost() {
        castFire(Map.of(player2.getId(), 2), List.of(player2.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void iceTapsTargetPermanentAndDrawsACard() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        harness.castModalInstant(player1, 0, 1, List.of(kavu.getId()));
        harness.passBothPriorities();

        assertThat(kavu.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void iceCannotTargetAPlayer() {
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fireDoesNotRedistributeDamageWhenOneTargetLeaves() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 1, kavu.getId(), 1),
                List.of(player2.getId(), kavu.getId()), List.of());
        gd.playerBattlefields.get(player2.getId()).remove(kavu);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void fireRejectsZeroDamageAssignedToATarget() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 2, kavu.getId(), 0),
                List.of(player2.getId(), kavu.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fireRejectsDamageAssignmentsThatDoNotTotalTwo() {
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void iceStillDrawsWhenTargetIsAlreadyTapped() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new KavuMauler());
        kavu.tap();
        harness.setLibrary(player1, List.of(new KavuMauler()));
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        harness.castModalInstant(player1, 0, 1, List.of(kavu.getId()));
        harness.passBothPriorities();

        assertThat(kavu.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void iceDoesNotDrawWhenItsOnlyTargetLeaves() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setLibrary(player1, List.of(new KavuMauler()));
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        harness.castModalInstant(player1, 0, 1, List.of(kavu.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(kavu);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({FireIce.class, MeddlingMage.class})
    void meddlingMageNamingFirePreventsCastingFire() {
        Permanent mage = harness.addToBattlefieldAndReturn(player2, new MeddlingMage());
        mage.setChosenName("Fire");
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({FireIce.class, MeddlingMage.class, KavuMauler.class})
    void meddlingMageNamingIcePreventsCastingIce() {
        Permanent mage = harness.addToBattlefieldAndReturn(player2, new MeddlingMage());
        mage.setChosenName("Ice");
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuMauler());
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.BLUE);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(kavu.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFire(Map<java.util.UUID, Integer> damageAssignments, List<java.util.UUID> targets) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FireIce()));
        addMana(ManaColor.RED);
        gs.playCard(gd, player1, 0, 0, null, damageAssignments, targets, List.of());
        harness.passBothPriorities();
    }

    private void addMana(ManaColor color) {
        harness.addMana(player1, color, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
