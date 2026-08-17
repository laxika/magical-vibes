package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DutifulThrull;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulExchangeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a Thrull returns a creature with two +1/+1 counters")
    void exiledThrullAddsCountersToReturnedCreature() {
        Card exiledCreature = new DutifulThrull();
        Card returnedCreature = new GrizzlyBears();
        Permanent exiledPermanent = harness.addToBattlefieldAndReturn(player1, exiledCreature);
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithSacrifice(player1, 0, returnedCreature.getId(), exiledPermanent.getId());
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(returnedCreature.getId()))
                .findFirst().orElseThrow();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.findExiledCard(exiledCreature.getId())).isNotNull();
    }

    @Test
    @DisplayName("Exiling a non-Thrull does not add counters")
    void exiledNonThrullDoesNotAddCounters() {
        Card exiledCreature = new GrizzlyBears();
        Card returnedCreature = new DutifulThrull();
        Permanent exiledPermanent = harness.addToBattlefieldAndReturn(player1, exiledCreature);
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithSacrifice(player1, 0, returnedCreature.getId(), exiledPermanent.getId());
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(returnedCreature.getId()))
                .findFirst().orElseThrow();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Requires a creature to be selected for the additional cost")
    void requiresCreatureForAdditionalCost() {
        Card returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new SoulExchange()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, returnedCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
