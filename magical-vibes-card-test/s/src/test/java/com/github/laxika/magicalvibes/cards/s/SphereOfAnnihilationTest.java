package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CircleOfTheMoonDruid;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfAnnihilation.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        JaceBeleren.class, MindStone.class, CircleOfTheMoonDruid.class})
class SphereOfAnnihilationTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with void counters equal to X")
    void entersWithXVoidCounters() {
        harness.setHand(player1, List.of(new SphereOfAnnihilation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent sphere = findPermanent(player1, "Sphere of Annihilation");
        assertThat(sphere.getCounterCount(CounterType.VOID)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger exiles qualifying permanents and graveyard cards")
    void upkeepExilesQualifyingPermanentsAndGraveyardCards() {
        Permanent sphere = addSphere(player1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        Card graveyardBears = new GrizzlyBears();
        Card graveyardJace = new JaceBeleren();
        Card graveyardAirElemental = new AirElemental();
        Card graveyardMindStone = new MindStone();
        harness.setGraveyard(player1, List.of(graveyardBears, graveyardAirElemental, graveyardMindStone));
        harness.setGraveyard(player2, List.of(graveyardJace));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphere, bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(jace);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(airElemental, mindStone);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(graveyardAirElemental, graveyardMindStone);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .contains(sphere.getCard(), bears.getCard(), jace.getCard(), graveyardBears, graveyardJace);
    }

    @Test
    void zeroXStillExilesSphereAtItsControllersUpkeep() {
        harness.setHand(player1, List.of(new SphereOfAnnihilation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();
        Permanent sphere = findPermanent(player1, "Sphere of Annihilation");
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new CircleOfTheMoonDruid());

        assertThat(sphere.getCounterCount(CounterType.VOID)).isZero();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphere);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(druid);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(sphere.getCard());
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent sphere = addSphere(player1, 3);
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new CircleOfTheMoonDruid());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sphere);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(druid);
    }

    @Test
    void usesCounterCountAtResolution() {
        Permanent sphere = addSphere(player1, 2);
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new CircleOfTheMoonDruid());

        advanceToUpkeep(player1);
        sphere.setCounterCount(CounterType.VOID, 3);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .contains(sphere.getCard(), druid.getCard());
    }

    @Test
    void usesLastKnownCountersWhenSphereLeavesBeforeResolution() {
        Permanent sphere = addSphere(player1, 2);
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new CircleOfTheMoonDruid());
        Card graveyardDruid = new CircleOfTheMoonDruid();
        harness.setGraveyard(player1, List.of(graveyardDruid));

        advanceToUpkeep(player1);
        sphere.setCounterCount(CounterType.VOID, 3);
        harness.getPermanentRemovalService().removePermanentToHand(gd, sphere);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(sphere.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(druid);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .contains(druid.getCard(), graveyardDruid).doesNotContain(sphere.getCard());
    }

    @Test
    void zeroCountersExileFaceDownCreaturesUsingTheirZeroManaValue() {
        Permanent sphere = addSphere(player1, 0);
        Permanent faceDownDruid = harness.addToBattlefieldAndReturn(player2, new CircleOfTheMoonDruid());
        faceDownDruid.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent faceUpDruid = harness.addToBattlefieldAndReturn(player2, new CircleOfTheMoonDruid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faceUpDruid).doesNotContain(faceDownDruid);
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .contains(sphere.getCard(), faceDownDruid.getCard());
    }

    private Permanent addSphere(Player owner, int voidCounters) {
        Permanent sphere = harness.addToBattlefieldAndReturn(owner, new SphereOfAnnihilation());
        sphere.setCounterCount(CounterType.VOID, voidCounters);
        sphere.setSummoningSick(false);
        return sphere;
    }
}
