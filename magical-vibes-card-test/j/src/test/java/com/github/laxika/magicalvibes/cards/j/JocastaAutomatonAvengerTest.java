package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.IronManArmoredAvenger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JocastaAutomatonAvenger.class, IronManArmoredAvenger.class})
class JocastaAutomatonAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when your commander deals combat damage")
    void commanderCombatDamagePutsCounterOnJocasta() {
        Permanent jocasta = addCreatureReady(player1, new JocastaAutomatonAvenger());
        Card commander = new IronManArmoredAvenger();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(jocasta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger from a noncommander dealing combat damage")
    void noncommanderCombatDamageDoesNotPutCounter() {
        Permanent jocasta = addCreatureReady(player1, new JocastaAutomatonAvenger());
        addCreatureReady(player1, new IronManArmoredAvenger());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(jocasta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May return itself from the graveyard tapped and attacking when your commander attacks")
    void commanderAttackReturnsJocastaTappedAndAttacking() {
        JocastaAutomatonAvenger jocasta = new JocastaAutomatonAvenger();
        harness.setGraveyard(player1, List.of(jocasta));
        Permanent commander = addCreatureReady(player1, new IronManArmoredAvenger());
        gd.makeCommander(player1.getId(), commander.getCard());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        Permanent returned = findPermanent(player1, "Jocasta, Automaton Avenger");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(jocasta.getId()));
    }

    @Test
    @DisplayName("Does not trigger from a noncommander attack")
    void noncommanderAttackDoesNotReturnJocasta() {
        JocastaAutomatonAvenger jocasta = new JocastaAutomatonAvenger();
        harness.setGraveyard(player1, List.of(jocasta));
        addCreatureReady(player1, new IronManArmoredAvenger());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(jocasta.getId()));
    }

    @Test
    @DisplayName("May decline returning Jocasta when your commander attacks")
    void mayDeclineReturn() {
        JocastaAutomatonAvenger jocasta = new JocastaAutomatonAvenger();
        harness.setGraveyard(player1, List.of(jocasta));
        Permanent commander = addCreatureReady(player1, new IronManArmoredAvenger());
        gd.makeCommander(player1.getId(), commander.getCard());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Jocasta, Automaton Avenger");
        harness.assertNotOnBattlefield(player1, "Jocasta, Automaton Avenger");
    }

    @Test
    @DisplayName("An opponent's commander you control does not give Jocasta a counter")
    void opposingCommanderCombatDamageDoesNotPutCounter() {
        Permanent jocasta = addCreatureReady(player1, new JocastaAutomatonAvenger());
        Card stolenCommander = new IronManArmoredAvenger();
        gd.makeCommander(player2.getId(), stolenCommander);
        addCreatureReady(player1, stolenCommander);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(jocasta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking with an opponent's commander does not return Jocasta")
    void opposingCommanderAttackDoesNotReturnJocasta() {
        harness.setGraveyard(player1, List.of(new JocastaAutomatonAvenger()));
        Card stolenCommander = new IronManArmoredAvenger();
        gd.makeCommander(player2.getId(), stolenCommander);
        addCreatureReady(player1, stolenCommander);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Jocasta, Automaton Avenger");
        harness.assertNotOnBattlefield(player1, "Jocasta, Automaton Avenger");
    }

    @Test
    @DisplayName("Return trigger survives the commander leaving combat before resolution")
    void commanderLeavingCombatDoesNotPreventReturn() {
        harness.setGraveyard(player1, List.of(new JocastaAutomatonAvenger()));
        Permanent commander = addCreatureReady(player1, new IronManArmoredAvenger());
        gd.makeCommander(player1.getId(), commander.getCard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        commander.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Jocasta, Automaton Avenger");
        harness.assertNotInGraveyard(player1, "Jocasta, Automaton Avenger");
    }

    @Test
    @DisplayName("Your commander dealing combat damage under another player's control still triggers Jocasta")
    void ownCommanderControlledByOpponentStillPutsCounter() {
        Permanent jocasta = addCreatureReady(player1, new JocastaAutomatonAvenger());
        Card commander = new IronManArmoredAvenger();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player2, commander);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(jocasta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
