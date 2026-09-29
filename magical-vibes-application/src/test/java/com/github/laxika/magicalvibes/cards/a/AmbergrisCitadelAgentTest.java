package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbergrisCitadelAgent.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Swamp.class})
class AmbergrisCitadelAgentTest extends BaseCardTest {

    @Test
    void baseFaceDiscardsItsHandAndDrawsTwoWhenItAttacks() {
        Permanent ambergris = addCreatureReady(player1, new AmbergrisCitadelAgent());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        attackAndAccept(ambergris, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void whiteFaceBoostsOtherCreaturesByCardsDiscardedThisTurn() {
        Permanent ambergris = specialize(CardColor.WHITE, 0, new Plains());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        attackAndAccept(ambergris, 2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ambergris)).isEqualTo(4);
    }

    @Test
    void blueFaceDrawsThreeCards() {
        Permanent ambergris = specialize(CardColor.BLUE, 1, new Island());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));

        attackAndAccept(ambergris, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
    }

    @Test
    void blackFaceGivesAnOpposingCreatureMinusTwoMinusTwo() {
        Permanent ambergris = specialize(CardColor.BLACK, 2, new Swamp());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        opposingCreature.tap();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        attackAndAccept(ambergris, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    void redFaceDealsDamageToEachOpponent() {
        Permanent ambergris = specialize(CardColor.RED, 3, new Mountain());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        attackAndAccept(ambergris, 2);
        resolveAllTriggers();

        harness.assertLife(player2, 13);
    }

    @Test
    void greenFacePutsCountersOnAnotherCreature() {
        Permanent ambergris = specialize(CardColor.GREEN, 4, new Forest());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        attackAndAccept(ambergris, 2);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        resolveAllTriggers();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent specialize(CardColor color, int abilityIndex, Card discardedCard) {
        harness.setHand(player1, List.of(new AmbergrisCitadelAgent(), discardedCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void attackAndAccept(Permanent ambergris, Integer discardCount) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ambergris)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (discardCount != null) {
            for (int i = 0; i < discardCount; i++) {
                harness.handleCardChosen(player1, 0);
            }
        }
    }
}
