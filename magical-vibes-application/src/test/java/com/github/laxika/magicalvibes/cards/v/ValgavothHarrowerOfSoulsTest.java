package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValgavothHarrowerOfSouls.class, GrizzlyBears.class, Shock.class})
class ValgavothHarrowerOfSoulsTest extends BaseCardTest {

    @Test
    void triggersForTheFirstLifeLossDuringAnOpponentsTurn() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(valgavoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void triggersOnlyOnceForAnOpponentDuringTheirTurn() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(valgavoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenAnOpponentLosesLifeDuringTheControllersTurn() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(valgavoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerIfTheOpponentAlreadyLostLifeBeforeValgavothEntered() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player2.getId());
        Permanent valgavoth = harness.enterBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(valgavoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    void wardCountersAnOpponentsSpellWhenTheyDeclineToPayLife() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, valgavoth.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(valgavoth.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void payingWardDuringTheOpponentsTurnTriggersTheLifeLossAbility() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, valgavoth.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(valgavoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(valgavoth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void targetingValgavothWithItsControllersSpellDoesNotTriggerWard() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, valgavoth.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(valgavoth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenItsControllerLosesLifeDuringAnOpponentsTurn() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothHarrowerOfSouls());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(valgavoth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }
}
