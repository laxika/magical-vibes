package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HamletbackGoliathTest extends BaseCardTest {

    private Permanent goliath() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Accepting the may puts X +1/+1 counters equal to the entering creature's power")
    void putsCountersEqualToEnteringPowerOnAccept() {
        harness.addToBattlefield(player1, new HamletbackGoliath());
        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Cast a 3/3 Hill Giant — Goliath's ability triggers for it entering.
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve Hill Giant spell → Goliath triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, goliath())).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, goliath())).isEqualTo(9);
    }

    @Test
    @DisplayName("Declining the may puts no counters")
    void noCountersOnDecline() {
        harness.addToBattlefield(player1, new HamletbackGoliath());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers for an opponent's creature entering")
    void triggersForOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new HamletbackGoliath());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when Hamletback Goliath itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.setHand(player1, List.of(new HamletbackGoliath()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve Goliath spell — it enters

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A power-0 creature entering yields zero counters even when accepted")
    void zeroPowerCreatureYieldsNoCounters() {
        harness.addToBattlefield(player1, new HamletbackGoliath());

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
