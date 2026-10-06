package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoadsideBlowout.class, AirResponseUnit.class, BirdsOfParadise.class,
        FountainOfYouth.class, GrizzlyBears.class})
class RoadsideBlowoutTest extends BaseCardTest {

    @Test
    void returnsManaValueOneCreatureForReducedCostAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Birds of Paradise");
        harness.assertInHand(player2, "Birds of Paradise");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotReduceCostForManaValueTwoCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void returnsTargetVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Air Response Unit");
        harness.assertInHand(player2, "Air Response Unit");
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle an opponent controls");
    }

    @Test
    void cannotTargetNonCreatureNonVehiclePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle an opponent controls");
    }

    @Test
    void cannotCastForOneBlueWhenTargetHasManaValueTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Roadside Blowout");
    }

    @Test
    void doesNotReduceCostForFaceDownCreatureWithManaValueZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInHand(player2, "Birds of Paradise");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Roadside Blowout");
        harness.assertInGraveyard(player2, "Birds of Paradise");
    }

    @Test
    void cannotTargetOwnUncrewedVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle an opponent controls");
    }

    @Test
    void doesNotDrawWhenTargetComesUnderCastersControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BirdsOfParadise());
        harness.setHand(player1, List.of(new RoadsideBlowout()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Birds of Paradise");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Roadside Blowout");
    }
}
