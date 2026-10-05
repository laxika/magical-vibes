package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.e.EbonyOwlNetsuke;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.cards.s.ShinenOfFlightsWings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JiwariTheEarthAflame.class, InnerChamberGuard.class, ArabaMothrider.class,
        EbonyOwlNetsuke.class, ShinenOfFlightsWings.class})
class JiwariTheEarthAflameTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsXDamageToTargetCreatureWithoutFlying() {
        Permanent jiwari = addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
        assertThat(jiwari.isTapped()).isTrue();
    }

    @Test
    void channelDealsXDamageToEachCreatureWithoutFlyingAndDiscardsSource() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        addCreatureReady(player1, new InnerChamberGuard());
        addCreatureReady(player1, new ArabaMothrider());
        addCreatureReady(player2, new InnerChamberGuard());
        addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jiwari, the Earth Aflame");
        harness.assertNotOnBattlefield(player1, "Inner-Chamber Guard");
        harness.assertNotOnBattlefield(player2, "Inner-Chamber Guard");
        harness.assertOnBattlefield(player1, "Araba Mothrider");
        harness.assertOnBattlefield(player2, "Araba Mothrider");
    }

    @Test
    void battlefieldAbilityCannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new ArabaMothrider());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    void battlefieldAbilityCannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EbonyOwlNetsuke());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void battlefieldAbilityAllowsZeroXAndStillTapsSource() {
        Permanent jiwari = addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(jiwari.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
    }

    @Test
    void battlefieldAbilityCannotBeActivatedWhileSummoningSick() {
        Permanent jiwari = harness.addToBattlefieldAndReturn(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(jiwari.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void channelMarksNonlethalDamageWithoutDamagingPlayersOrArtifacts() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        Permanent ownCreature = addCreatureReady(player1, new InnerChamberGuard());
        Permanent opposingCreature = addCreatureReady(player2, new InnerChamberGuard());
        Permanent flyer = addCreatureReady(player2, new ArabaMothrider());
        harness.addToBattlefield(player2, new EbonyOwlNetsuke());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null, 1);

        harness.assertNotInHand(player1, "Jiwari, the Earth Aflame");
        harness.assertInGraveyard(player1, "Jiwari, the Earth Aflame");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyer.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Inner-Chamber Guard");
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
        harness.assertOnBattlefield(player2, "Ebony Owl Netsuke");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void channelAllowsZeroXAndStillDiscardsSource() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        Permanent creature = addCreatureReady(player2, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateHandAbility(player1, 0, null, 0);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Jiwari, the Earth Aflame");
        harness.assertInGraveyard(player1, "Jiwari, the Earth Aflame");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
    }

    @Test
    void channelRequiresThreeRedManaInAdditionToX() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 2))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jiwari, the Earth Aflame");
        harness.assertNotInGraveyard(player1, "Jiwari, the Earth Aflame");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void battlefieldAbilityDoesNotDamageTargetThatGainsFlyingInResponse() {
        addCreatureReady(player1, new JiwariTheEarthAflame());
        Permanent target = addCreatureReady(player2, new InnerChamberGuard());
        harness.setHand(player2, List.of(new ShinenOfFlightsWings()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.activateHandAbility(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
    }

    @Test
    void channelChecksFlyingWhenItResolves() {
        harness.setHand(player1, List.of(new JiwariTheEarthAflame()));
        harness.setHand(player2, List.of(new ShinenOfFlightsWings()));
        Permanent savedCreature = addCreatureReady(player2, new InnerChamberGuard());
        addCreatureReady(player1, new InnerChamberGuard());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.activateHandAbility(player2, 0, savedCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(savedCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
        harness.assertNotOnBattlefield(player1, "Inner-Chamber Guard");
    }
}
