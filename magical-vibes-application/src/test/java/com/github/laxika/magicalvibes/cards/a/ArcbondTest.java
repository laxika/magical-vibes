package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CeruleanDrake;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulfireGrandMaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Arcbond.class, AirElemental.class, Shock.class, Forest.class,
        CeruleanDrake.class, SoulfireGrandMaster.class})
class ArcbondTest extends BaseCardTest {

    @Test
    @DisplayName("The watched creature deals that much damage to each other creature and each player")
    void dealsDamageToOtherCreaturesAndPlayers() {
        Permanent watched = addCreatureReady(player2, new AirElemental());
        Permanent friendly = addCreatureReady(player1, new AirElemental());
        Permanent opposing = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new Arcbond(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.passBothPriorities();

        assertThat(watched.getMarkedDamage()).isEqualTo(2);
        assertThat(friendly.getMarkedDamage()).isEqualTo(2);
        assertThat(opposing.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The delayed trigger expires at end of turn")
    void triggerExpiresAtEndOfTurn() {
        Permanent watched = addCreatureReady(player2, new AirElemental());
        Permanent other = addCreatureReady(player1, new AirElemental());

        harness.setHand(player1, List.of(new Arcbond()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, watched.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.passBothPriorities();

        assertThat(watched.getMarkedDamage()).isEqualTo(2);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        UUID forestId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();

        harness.setHand(player1, List.of(new Arcbond()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each damage event triggers, including the event that kills the watched creature")
    void triggersForRepeatedAndLethalDamage() {
        Permanent watched = addCreatureReady(player2, new AirElemental());
        addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new Arcbond(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, watched.getId());

        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);

        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Protection checks the watched creature's color rather than Arcbond's color")
    void blueCreatureDamagesCreatureWithProtectionFromRed() {
        Permanent watched = addCreatureReady(player2, new AirElemental());
        addCreatureReady(player1, new CeruleanDrake());
        harness.setHand(player1, List.of(new Arcbond(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cerulean Drake");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A dead watched creature's lifelink benefits its controller rather than Arcbond's caster")
    void deadOpposingCreatureGainsLifeForItsController() {
        Permanent watched = addCreatureReady(player2, new SoulfireGrandMaster());
        Permanent other = addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new Arcbond(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.castAndResolveInstant(player1, 0, watched.getId());
        harness.assertInGraveyard(player2, "Soulfire Grand Master");
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 24);
    }
}
