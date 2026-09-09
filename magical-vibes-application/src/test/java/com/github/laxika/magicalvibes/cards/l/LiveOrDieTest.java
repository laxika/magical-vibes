package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiveOrDie.class, GrizzlyBears.class, HolyDay.class, Millstone.class})
class LiveOrDieTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureFromGraveyardToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotReturnNonCreatureCardFromGraveyard() {
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotDestroyNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                harness.getPermanentId(player2, "Millstone")))
                .isInstanceOf(IllegalStateException.class);
    }
}
