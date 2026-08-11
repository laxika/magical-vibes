package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OmnathLocusOfTheRoilTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter trigger deals damage equal to the number of Elementals controlled")
    void enterTriggerCountsItselfAndOtherElementals() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());

        castOmnath();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on a targeted Elemental and draws at eight lands")
    void landfallCountersTargetAndDrawsAtEightLands() {
        Permanent elemental = addOmnathAndElemental(7);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Landfall still counters an Elemental below eight lands without drawing")
    void landfallBelowEightLandsDoesNotDraw() {
        Permanent elemental = addOmnathAndElemental(0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Landfall cannot target a non-Elemental creature")
    void landfallRejectsNonElementalTarget() {
        Permanent nonElemental = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addOmnathAndElemental(0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addOmnathAndElemental(int landCount) {
        harness.addToBattlefield(player1, new OmnathLocusOfTheRoil());
        harness.addToBattlefield(player1, new AirElemental());
        for (int i = 0; i < landCount; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AirElemental)
                .findFirst()
                .orElseThrow();
    }

    private void castOmnath() {
        harness.setHand(player1, List.of(new OmnathLocusOfTheRoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
    }
}
