package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HamletbackGoliath.class, HillGiant.class, GrizzlyBears.class, Ornithopter.class,
        GiantGrowth.class, GloriousAnthem.class})
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
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");

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

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");

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

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

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
        harness.castFromHand(player1, new HamletbackGoliath(), "{6}{R}");

        harness.passBothPriorities(); // resolve Goliath spell — it enters

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A power-0 creature entering yields zero counters even when accepted")
    void zeroPowerCreatureYieldsNoCounters() {
        harness.addToBattlefield(player1, new HamletbackGoliath());

        harness.castFromHand(player1, new Ornithopter(), "{0}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    @Test
    @DisplayName("Uses the entering creature's power when the ability resolves")
    void usesPowerAfterResponseToTrigger() {
        harness.addToBattlefield(player1, new HamletbackGoliath());
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(6);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Includes continuous power modifiers on the entering creature")
    void includesContinuousPowerModifiers() {
        harness.addToBattlefield(player1, new HamletbackGoliath());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player1.getId()).get(2);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goliath().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
