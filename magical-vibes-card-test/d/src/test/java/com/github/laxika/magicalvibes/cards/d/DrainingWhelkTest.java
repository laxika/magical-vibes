package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.Conflagrate;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.SquallLine;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrainingWhelk.class, BenalishCavalry.class, PrismaticLens.class, SquallLine.class,
        Cancel.class, Conflagrate.class, SuddenDeath.class})
class DrainingWhelkTest extends BaseCardTest {

    @Test
    void entersWithoutCountersWhenThereIsNoSpellToTarget() {
        harness.setHand(player1, List.of(new DrainingWhelk()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Draining Whelk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsEachXSymbolInTheTargetSpellsManaCost() {
        Conflagrate conflagrate = new Conflagrate();
        harness.setHand(player1, List.of(conflagrate));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.setHand(player2, List.of(new DrainingWhelk()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castSorceryForX(player1, 0, 3, Map.of(player2.getId(), 3));
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, conflagrate.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Conflagrate");
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player2, "Draining Whelk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void stillCountersSpellAfterWhelkLeavesBattlefield() {
        PrismaticLens lens = new PrismaticLens();
        harness.setHand(player1, List.of(lens, new SuddenDeath()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new DrainingWhelk()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, lens.getId());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Draining Whelk"));
        harness.assertInGraveyard(player2, "Draining Whelk");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prismatic Lens");
        harness.assertNotOnBattlefield(player1, "Prismatic Lens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void getsNoCountersWhenTargetSpellLeavesStackBeforeTriggerResolves() {
        PrismaticLens lens = new PrismaticLens();
        harness.setHand(player1, List.of(lens, new Cancel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new DrainingWhelk()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, lens.getId());
        harness.castAndResolveInstant(player1, 0, lens.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prismatic Lens");
        assertThat(findPermanent(player2, "Draining Whelk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

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
