package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SHIELDDeploymentDrone.class})
class SHIELDDeploymentDroneTest extends BaseCardTest {

    @Test
    @DisplayName("S.H.I.E.L.D. Deployment Drone creates a 1/1 white Soldier when it enters")
    void createsSoldierWhenItEnters() {
        harness.setHand(player1, List.of(new SHIELDDeploymentDrone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("The Soldier is created by a separate enters trigger, not by casting the Drone")
    void createsTokenOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new SHIELDDeploymentDrone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        assertThat(countPermanents(player1, "Soldier")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "S.H.I.E.L.D. Deployment Drone")).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isZero();

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();
        assertThat(findPermanent(player1, "Soldier").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast creates a Soldier for the Drone's controller")
    void nonCastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new SHIELDDeploymentDrone());

        resolveAllTriggers();

        assertThat(countPermanents(player2, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }
}
