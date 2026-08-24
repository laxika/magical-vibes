package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({FluxChanneler.class, GrizzlyBears.class, Spellbook.class})
class FluxChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell triggers proliferate")
    void noncreatureSpellTriggersProliferate() {
        harness.addToBattlefield(player1, new FluxChanneler());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Flux Channeler"));

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger proliferate")
    void creatureSpellDoesNotTriggerProliferate() {
        harness.addToBattlefield(player1, new FluxChanneler());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not trigger proliferate")
    void opponentNoncreatureSpellDoesNotTriggerProliferate() {
        harness.addToBattlefield(player1, new FluxChanneler());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isNotEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
