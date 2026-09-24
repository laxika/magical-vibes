package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JocastaAutomatonAvenger.class, GrizzlyBears.class})
class JocastaAutomatonAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when your commander deals combat damage")
    void commanderCombatDamagePutsCounterOnJocasta() {
        Permanent jocasta = addCreatureReady(player1, new JocastaAutomatonAvenger());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        declareAttackersAndPrepareBlockers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(jocasta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger from a noncommander dealing combat damage")
    void noncommanderCombatDamageDoesNotPutCounter() {
        Permanent jocasta = addCreatureReady(player1, new JocastaAutomatonAvenger());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        resolveCombat();

        assertThat(jocasta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May return itself from the graveyard tapped and attacking when your commander attacks")
    void commanderAttackReturnsJocastaTappedAndAttacking() {
        JocastaAutomatonAvenger jocasta = new JocastaAutomatonAvenger();
        harness.setGraveyard(player1, List.of(jocasta));
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), commander.getCard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(jocasta.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(jocasta.getId()));
    }

    @Test
    @DisplayName("Does not trigger from a noncommander attack")
    void noncommanderAttackDoesNotReturnJocasta() {
        JocastaAutomatonAvenger jocasta = new JocastaAutomatonAvenger();
        harness.setGraveyard(player1, List.of(jocasta));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(jocasta.getId()));
    }
}
