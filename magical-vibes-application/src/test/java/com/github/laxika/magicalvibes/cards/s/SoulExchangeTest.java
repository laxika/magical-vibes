package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulExchange.class, BasalThrull.class, RiverMerfolk.class})
class SoulExchangeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a Thrull returns a creature with a +2/+2 counter")
    void exiledThrullAddsCountersToReturnedCreature() {
        Card exiledCreature = new BasalThrull();
        Card returnedCreature = new RiverMerfolk();
        Permanent exiledPermanent = harness.addToBattlefieldAndReturn(player1, exiledCreature);
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithSacrifice(player1, 0, returnedCreature.getId(), exiledPermanent.getId());
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, returnedCreature.getName());
        assertThat(permanent.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isEqualTo(1);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(exiledCreature.getId())).isNotNull();
    }

    @Test
    @DisplayName("Exiling a non-Thrull does not add counters")
    void exiledNonThrullDoesNotAddCounters() {
        Card exiledCreature = new RiverMerfolk();
        Card returnedCreature = new BasalThrull();
        Permanent exiledPermanent = harness.addToBattlefieldAndReturn(player1, exiledCreature);
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithSacrifice(player1, 0, returnedCreature.getId(), exiledPermanent.getId());
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, returnedCreature.getName());
        assertThat(permanent.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO)).isZero();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Requires a creature to be selected for the additional cost")
    void requiresCreatureForAdditionalCost() {
        Card returnedCreature = new RiverMerfolk();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, returnedCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCardInGraveyard() {
        Card exiledCreature = new BasalThrull();
        Card noncreatureCard = new SoulExchange();
        Permanent exiledPermanent = harness.addToBattlefieldAndReturn(player1, exiledCreature);
        harness.setGraveyard(player1, List.of(noncreatureCard));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, noncreatureCard.getId(), exiledPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card exiledCreature = new BasalThrull();
        Card returnedCreature = new RiverMerfolk();
        Permanent exiledPermanent = harness.addToBattlefieldAndReturn(player1, exiledCreature);
        harness.setGraveyard(player2, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, returnedCreature.getId(), exiledPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
