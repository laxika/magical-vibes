package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoundingWolf;
import com.github.laxika.magicalvibes.cards.p.PestilentWolf;
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

@CardUsed({ContortionistTroupe.class, PestilentWolf.class, BoundingWolf.class})
class ContortionistTroupeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new ContortionistTroupe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent troupe = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troupe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Coven puts a +1/+1 counter on a target creature you control at your end step")
    void covenPutsCounterOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new BoundingWolf());
        harness.setHand(player1, List.of(new ContortionistTroupe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Coven does not trigger without three different powers")
    void doesNotTriggerWithoutCoven() {
        harness.addToBattlefield(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new PestilentWolf());
        harness.setHand(player1, List.of(new ContortionistTroupe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent troupe = gd.playerBattlefields.get(player1.getId()).getLast();

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(troupe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Coven target selection excludes creatures controlled by an opponent")
    void targetSelectionExcludesOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new BoundingWolf());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PestilentWolf());
        harness.setHand(player1, List.of(new ContortionistTroupe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent troupe = gd.playerBattlefields.get(player1.getId()).getLast();

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(troupe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("X zero enters without counters and dies")
    void zeroXDies() {
        harness.setHand(player1, List.of(new ContortionistTroupe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof ContortionistTroupe);
    }

    @Test
    @DisplayName("Coven checks different powers again when the ability resolves")
    void covenLostBeforeResolution() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new BoundingWolf());
        Permanent troupe = castTroupeWithOneCounter();

        advanceToEndStep();
        harness.handlePermanentChosen(player1, wolf.getId());
        troupe.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Coven can target the Troupe itself despite other duplicate powers")
    void canTargetItselfWithDuplicatePowersPresent() {
        harness.addToBattlefield(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new BoundingWolf());
        Permanent troupe = castTroupeWithOneCounter();

        advanceToEndStep();
        harness.handlePermanentChosen(player1, troupe.getId());
        harness.passBothPriorities();

        assertThat(troupe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Coven does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new PestilentWolf());
        harness.addToBattlefield(player1, new BoundingWolf());
        Permanent troupe = castTroupeWithOneCounter();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(troupe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to coven")
    void opponentCreaturesDoNotEnableCoven() {
        harness.addToBattlefield(player1, new PestilentWolf());
        harness.addToBattlefield(player2, new BoundingWolf());
        Permanent troupe = castTroupeWithOneCounter();

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(troupe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    private Permanent castTroupeWithOneCounter() {
        harness.setHand(player1, List.of(new ContortionistTroupe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
