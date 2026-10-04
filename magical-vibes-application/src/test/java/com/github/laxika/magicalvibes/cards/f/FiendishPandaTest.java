package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KembasSkyguard;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiendishPanda.class, CentaurCourser.class, GrizzlyBears.class, HillGiant.class, KembasSkyguard.class, WrathOfGod.class})
class FiendishPandaTest extends BaseCardTest {

    @Test
    void gainingLifePutsCounterOnFiendishPanda() {
        Card panda = new FiendishPanda();
        harness.addToBattlefield(player1, panda);
        harness.setHand(player1, List.of(new KembasSkyguard()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fiendish Panda").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void deathReturnsEligibleNonBearCreatureCardToBattlefield() {
        Card panda = new FiendishPanda();
        Card eligible = new CentaurCourser();
        Card bear = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.addToBattlefield(player1, panda);
        harness.setGraveyard(player1, List.of(eligible, bear, tooExpensive));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertInGraveyard(player1, "Fiendish Panda");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }
    @Test
    void separateLifeGainEventsEachPutOneCounterOnPanda() {
        harness.addToBattlefield(player1, new FiendishPanda());
        harness.setHand(player1, List.of(new KembasSkyguard(), new KembasSkyguard()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        for (int i = 0; i < 2; i++) {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(findPermanent(player1, "Fiendish Panda").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void opponentsLifeGainDoesNotPutCounterOnPanda() {
        harness.addToBattlefield(player1, new FiendishPanda());
        harness.setHand(player2, List.of(new KembasSkyguard()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fiendish Panda").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    void deathUsesPowerIncludingLifeGainCounter() {
        harness.addToBattlefield(player1, new FiendishPanda());
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));
        harness.setHand(player1, List.of(new KembasSkyguard(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Fiendish Panda");
    }

    @Test
    void deathCanReturnCreatureThatDiesSimultaneouslyButNotOpponentsCard() {
        harness.addToBattlefield(player1, new FiendishPanda());
        Card ownCreature = new CentaurCourser();
        Card opponentsCreature = new CentaurCourser();
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player2, opponentsCreature);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertInGraveyard(player2, "Centaur Courser");
        harness.assertNotOnBattlefield(player2, "Centaur Courser");
    }
}
