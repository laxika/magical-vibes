package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoshIronGolem;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.MyrEnforcer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElectrostaticBolt.class, BoshIronGolem.class, FangrenHunter.class, LeoninScimitar.class,
        MyrEnforcer.class})
class ElectrostaticBoltTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToNonArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FangrenHunter());
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void dealsFourDamageToArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoshIronGolem());
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysArtifactCreatureWithFourToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MyrEnforcer());
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Myr Enforcer");
        harness.assertInGraveyard(player2, "Myr Enforcer");
        harness.assertInGraveyard(player1, "Electrostatic Bolt");
    }

    @Test
    void canDamageCreatureControlledByCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FangrenHunter());
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Fangren Hunter");
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
