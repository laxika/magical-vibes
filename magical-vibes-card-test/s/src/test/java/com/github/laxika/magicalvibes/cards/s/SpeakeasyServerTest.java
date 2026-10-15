package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.j.Jackhammer;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpeakeasyServer.class, ChromeCat.class, Jackhammer.class, Murder.class})
class SpeakeasyServerTest extends BaseCardTest {

    @Test
    void entersAndGainsLifeForEachOtherCreatureYouControl() {
        harness.addToBattlefield(player1, new ChromeCat());
        harness.addToBattlefield(player1, new ChromeCat());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SpeakeasyServer(), "{4}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void doesNotCountCreaturesControlledByAnOpponentOrItself() {
        harness.addToBattlefield(player2, new ChromeCat());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SpeakeasyServer(), "{4}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void doesNotCountNoncreaturePermanents() {
        harness.addToBattlefield(player1, new Jackhammer());
        harness.addToBattlefield(player1, new ChromeCat());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SpeakeasyServer(), "{4}{W}");

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void countsCreaturesAtResolutionAfterAnotherCreatureIsDestroyed() {
        var cat = harness.addToBattlefieldAndReturn(player1, new ChromeCat());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SpeakeasyServer(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, cat.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof ChromeCat);
    }

    @Test
    void triggerStillGainsLifeAfterServerIsDestroyed() {
        harness.addToBattlefield(player1, new ChromeCat());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SpeakeasyServer(), "{4}{W}");
        harness.passBothPriorities();
        var server = findPermanent(player1, "Speakeasy Server");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, server.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof SpeakeasyServer);
    }
}
