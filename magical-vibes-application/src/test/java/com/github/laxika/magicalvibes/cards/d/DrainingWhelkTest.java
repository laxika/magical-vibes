package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.SquallLine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrainingWhelk.class, BenalishCavalry.class, PrismaticLens.class, SquallLine.class})
class DrainingWhelkTest extends BaseCardTest {

    @Test
    void countersTargetCreatureSpellAndGetsCountersEqualToItsManaValue() {
        BenalishCavalry cavalry = new BenalishCavalry();
        harness.setHand(player1, List.of(cavalry));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new DrainingWhelk()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, cavalry.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Cavalry");
        assertThat(findPermanent(player2, "Draining Whelk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canCounterNoncreatureSpell() {
        PrismaticLens lens = new PrismaticLens();
        harness.setHand(player1, List.of(lens));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new DrainingWhelk()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, lens.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prismatic Lens");
        assertThat(findPermanent(player2, "Draining Whelk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void usesChosenXWhenCountingCountersForXSpell() {
        SquallLine squallLine = new SquallLine();
        harness.setHand(player1, List.of(squallLine));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new DrainingWhelk()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, 2, null);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, squallLine.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Draining Whelk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Squall Line");
    }
}
