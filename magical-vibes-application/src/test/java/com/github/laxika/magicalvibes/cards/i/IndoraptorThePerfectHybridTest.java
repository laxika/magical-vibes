package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndoraptorThePerfectHybrid.class, GrizzlyBears.class, Shock.class})
class IndoraptorThePerfectHybridTest extends BaseCardTest {

    @Test
    void bloodthirstXUsesDamageDealtToOpponentsThisTurn() {
        gd.recordDamageToPlayer(player2.getId(), 4);
        castIndoraptor();

        assertThat(findPermanent(player1, "Indoraptor, the Perfect Hybrid")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void enrageOffersSacrificeToTheRandomOpponent() {
        Permanent indoraptor = harness.addToBattlefieldAndReturn(player1, new IndoraptorThePerfectHybrid());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        shockIndoraptor(indoraptor);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enrageDealsDamageEqualToPowerWhenOpponentDeclinesSacrifice() {
        Permanent indoraptor = harness.addToBattlefieldAndReturn(player1, new IndoraptorThePerfectHybrid());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        indoraptor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        shockIndoraptor(indoraptor);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(findPermanent(player2, "Grizzly Bears")).isSameAs(bears);
    }

    @Test
    void enrageDealsDamageWithoutOfferingChoiceWhenNoCreatureCanBeSacrificed() {
        Permanent indoraptor = harness.addToBattlefieldAndReturn(player1, new IndoraptorThePerfectHybrid());
        shockIndoraptor(indoraptor);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castIndoraptor() {
        harness.setHand(player1, List.of(new IndoraptorThePerfectHybrid()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void shockIndoraptor(Permanent indoraptor) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, indoraptor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
