package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempestAngler.class, Shock.class, GrizzlyBears.class})
class TempestAnglerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on Tempest Angler")
    void noncreatureSpellAddsCounter() {
        harness.addToBattlefield(player1, new TempestAngler());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent angler = getAngler();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(angler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Tempest Angler")
    void creatureSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new TempestAngler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent angler = getAngler();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(angler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not trigger Tempest Angler")
    void opponentNoncreatureSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new TempestAngler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent angler = getAngler();
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(angler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent getAngler() {
        return findPermanent(player1, "Tempest Angler");
    }
}
