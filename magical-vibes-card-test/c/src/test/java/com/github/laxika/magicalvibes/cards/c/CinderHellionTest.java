package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderHellion.class, ElspethKnightErrant.class, GrizzlyBears.class})
class CinderHellionTest extends BaseCardTest {

    @Test
    void entersBeforeChoosingEtbTarget() {
        harness.castFromHand(player1, new CinderHellion(), "{4}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cinder Hellion");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
    }

    @Test
    void dealsTwoDamageToTargetOpponent() {
        harness.setLife(player2, 20);
        castAndChoose(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsTwoDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        castAndChoose(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void offersOnlyOpponentsAndPlaneswalkers() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCinderHellion();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId())
                .doesNotContain(player1.getId(), creature.getId());
    }

    @Test
    void canDamageItsControllersPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        castAndChoose(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void destroysPlaneswalkerWithTwoLoyaltyWithoutDamagingItsController() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        castAndChoose(planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Elspeth, Knight-Errant");
        harness.assertInGraveyard(player2, "Elspeth, Knight-Errant");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotRedirectDamageWhenTargetPlaneswalkerLeaves() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        castCinderHellion();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggeredDamageResolvesAfterCinderHellionLeaves() {
        castCinderHellion();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Cinder Hellion");
    }

    private void castAndChoose(java.util.UUID targetId) {
        castCinderHellion();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void castCinderHellion() {
        harness.castFromHand(player1, new CinderHellion(), "{4}{R}");
        harness.passBothPriorities();
    }
}
