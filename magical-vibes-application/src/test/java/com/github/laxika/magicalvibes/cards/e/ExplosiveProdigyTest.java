package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExplosiveProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Vivid deals one damage when it is the only color among your permanents")
    void dealsOneDamageForOneColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castExplosiveProdigy(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Vivid deals damage equal to the distinct colors among your permanents")
    void dealsDamageForDistinctColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castExplosiveProdigy(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Serra Angel").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Vivid counts only permanents controlled by its controller")
    void ignoresOpponentColors() {
        harness.addToBattlefield(player2, new AirElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castExplosiveProdigy(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Serra Angel").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Vivid cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ExplosiveProdigy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castExplosiveProdigy(Permanent target) {
        harness.setHand(player1, List.of(new ExplosiveProdigy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, target.getId());
    }
}
