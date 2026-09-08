package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnapdaxApexOfTheHunt.class, GrizzlyBears.class, ChandraBoldPyromancer.class})
class SnapdaxApexOfTheHuntTest extends BaseCardTest {

    @Test
    void mutatingDealsFourDamageToOpponentsCreatureAndGainsFourLife() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 24);
    }

    @Test
    void mutatingDealsFourDamageToOpponentsPlaneswalkerAndGainsFourLife() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent planeswalker = new Permanent(new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 24);
    }

    @Test
    void mutatingCannotTargetOwnCreature() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(snapdax);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void triggerMutation(Permanent snapdax) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, snapdax, List.of(snapdax.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
