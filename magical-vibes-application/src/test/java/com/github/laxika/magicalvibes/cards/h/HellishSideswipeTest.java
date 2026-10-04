package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellishSideswipe.class, AirResponseUnit.class, Forest.class, Frogify.class, GrizzlyBears.class})
class HellishSideswipeTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndDestroysTargetCreatureWithoutDrawing() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenTheSacrificedPermanentWasAVehicle() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Air Response Unit");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canDestroyATargetVehicle() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Response Unit");
    }

    @Test
    void cannotTargetNoncreatureNonVehiclePermanent() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrificed.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    void cannotSacrificeALandThatIsNeitherArtifactNorCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice target must be an artifact or creature");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Hellish Sideswipe");
    }

    @Test
    void doesNotDrawWhenItsOnlyTargetWasSacrificedToPayTheCost() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, vehicle.getId(), vehicle.getId());
        harness.assertInGraveyard(player1, "Air Response Unit");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hellish Sideswipe");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForASacrificedVehicleThatHadLostItsVehicleSubtype() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, vehicle.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HellishSideswipe()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), vehicle.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Air Response Unit");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
