package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.cards.t.TurntimberAscetic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilEruption.class, GrizzlyBears.class, Forest.class, JaceMirrorMage.class,
        TurntimberAscetic.class})
class RoilEruptionTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutKicker() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void dealsFiveDamageWhenKicked() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void canDealDamageToACreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void unkickedDamageDoesNotKillFourToughnessCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new TurntimberAscetic());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Turntimber Ascetic");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void kickedDamageKillsFourToughnessCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new TurntimberAscetic());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedSorceryWithTap(player1, 0, creature.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Turntimber Ascetic");
        harness.assertInGraveyard(player2, "Turntimber Ascetic");
    }

    @Test
    void canDamageItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void dealsThreeDamageToPlaneswalker() {
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        harness.assertOnBattlefield(player2, "Jace, Mirror Mage");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void kickedDamageRemovesPlaneswalker() {
        var planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedSorceryWithTap(player1, 0, planeswalker.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace, Mirror Mage");
        harness.assertInGraveyard(player2, "Jace, Mirror Mage");
    }

    @Test
    void cannotTargetAnUnanimatedLand() {
        var land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RoilEruption()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Roil Eruption");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
