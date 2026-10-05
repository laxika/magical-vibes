package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.r.RalCracklingWit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlayfulShove.class, GrizzlyBears.class, BarkformHarvester.class, RalCracklingWit.class})
class PlayfulShoveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature and draws a card")
    void damagesCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlayfulShove()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 1 damage to a player and draws a card")
    void damagesPlayerAndDrawsCard() {
        harness.setHand(player1, List.of(new PlayfulShove()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw a card if its target is removed before resolution")
    void fizzlingDoesNotDrawCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PlayfulShove()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        gd.playerBattlefields.get(player2.getId()).clear();
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast);
    }

    @Test
    @DisplayName("Can damage its controller and draws exactly one card")
    void damagesControllerAndDrawsOneCard() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PlayfulShove()));
        harness.setLibrary(player1, List.of(new BarkformHarvester(), new BarkformHarvester()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Barkform Harvester");
        harness.assertInGraveyard(player1, "Playful Shove");
    }

    @Test
    @DisplayName("Removes one loyalty from a planeswalker and draws a card")
    void damagesPlaneswalkerAndDrawsCard() {
        var ral = harness.addToBattlefieldAndReturn(player2, new RalCracklingWit());
        ral.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new PlayfulShove()));
        harness.setLibrary(player1, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, ral.getId());

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInHand(player1, "Barkform Harvester");
    }

    @Test
    @DisplayName("Still draws when the damage is lethal to the targeted creature")
    void lethalDamageStillDrawsCard() {
        var creature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());
        creature.setMarkedDamage(2);
        harness.setHand(player1, List.of(new PlayfulShove()));
        harness.setLibrary(player1, List.of(new BarkformHarvester()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Barkform Harvester");
        harness.assertInGraveyard(player2, "Barkform Harvester");
        harness.assertInHand(player1, "Barkform Harvester");
    }
}
