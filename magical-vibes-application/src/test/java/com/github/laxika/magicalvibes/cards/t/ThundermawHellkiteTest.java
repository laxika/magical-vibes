package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThundermawHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Entering deals 1 damage to each flying creature opponents control and taps them")
    void entersDamagesAndTapsOpponentFliers() {
        Permanent oppFlier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent oppGround = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castHellkite();

        assertThat(oppFlier.getMarkedDamage()).isEqualTo(1);
        assertThat(oppFlier.isTapped()).isTrue();
        assertThat(oppGround.getMarkedDamage()).isZero();
        assertThat(oppGround.isTapped()).isFalse();
        assertThat(ownFlier.getMarkedDamage()).isZero();
        assertThat(ownFlier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A 1/1 flier an opponent controls dies to the enter trigger")
    void killsOneToughnessFliers() {
        harness.addToBattlefield(player2, new SuntailHawk());

        castHellkite();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    private void castHellkite() {
        harness.setHand(player1, List.of(new ThundermawHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the enter trigger
    }
}
