package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BearUmbra;
import com.github.laxika.magicalvibes.cards.e.EiganjoExemplar;
import com.github.laxika.magicalvibes.cards.j.JovensFerrets;
import com.github.laxika.magicalvibes.cards.k.KaitoShizuki;
import com.github.laxika.magicalvibes.cards.o.OathOfKaya;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsshinTwoHeavensAsOne.class, JovensFerrets.class, Roterothopter.class,
        BearUmbra.class, EiganjoExemplar.class, KaitoShizuki.class, OathOfKaya.class})
class IsshinTwoHeavensAsOneTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles attack triggers of permanents you control")
    void doublesAttackTriggers() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(ferrets.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not double non-attack triggers")
    void doesNotDoubleNonAttackTriggers() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());
        ferrets.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Roterothopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void doublesAnotherPermanentsTriggerWhenIsshinAttacks() {
        Permanent isshin = addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        addCreatureReady(player1, new EiganjoExemplar());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(isshin.getPowerModifier()).isEqualTo(2);
        assertThat(isshin.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void doesNotDoubleOpponentsAttackTriggers() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent ferrets = addCreatureReady(player2, new JovensFerrets());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(ferrets.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void doublesAttackAbilityGrantedToIsshin() {
        Permanent isshin = addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BearUmbra());
        aura.setAttachedTo(isshin.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
    }

    @Test
    void doublesAttackAbilityGrantedToAnotherCreature() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent exemplar = addCreatureReady(player1, new EiganjoExemplar());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BearUmbra());
        aura.setAttachedTo(exemplar.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
    }

    @Test
    void doublesTriggersCausedByOpponentAttackingPlaneswalker() {
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new KaitoShizuki());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player1, new OathOfKaya());
        addCreatureReady(player2, new EiganjoExemplar());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
            resolveAllTriggers();
        });

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
