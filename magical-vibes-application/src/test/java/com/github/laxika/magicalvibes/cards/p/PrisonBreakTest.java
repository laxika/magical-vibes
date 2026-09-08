package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AbandonHope;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrisonBreak.class, GrizzlyBears.class, AbandonHope.class})
class PrisonBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature with a +1/+1 counter")
    void returnsCreatureWithCounter() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PrisonBreak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Cannot target a non-creature card")
    void cannotTargetNonCreatureCard() {
        Card nonCreature = new AbandonHope();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new PrisonBreak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mayhem casts it from the graveyard for {3}{B} after it was discarded")
    void mayhemCastReturnsCreatureAndKeepsSpellInGraveyard() {
        Card creature = new GrizzlyBears();
        Card prisonBreak = new PrisonBreak();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AbandonHope(), prisonBreak));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithDiscards(player1, 0, 1, player2.getId(), List.of(1));
        harness.passBothPriorities();

        int prisonBreakIndex = gd.playerGraveyards.get(player1.getId()).indexOf(prisonBreak);
        harness.castFromGraveyardTargeting(player1, prisonBreakIndex, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(prisonBreak);
    }

    @Test
    @DisplayName("Cannot cast it from the graveyard unless it was discarded this turn")
    void mayhemRequiresDiscardThisTurn() {
        Card creature = new GrizzlyBears();
        Card prisonBreak = new PrisonBreak();
        harness.setGraveyard(player1, List.of(creature, prisonBreak));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
