package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalSpring;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornfistStriker.class, GrizzlyBears.class, Shock.class, NaturalSpring.class,
        ProdigalPyromancer.class})
class ThornfistStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, striker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(striker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("No anthem when you have not gained life this turn")
    void noAnthemWithoutLifeGain() {
        harness.addToBattlefield(player1, new ThornfistStriker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Other creatures you control get +1/+0 and trample while you have gained life")
    void anthemWhileLifeGained() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        // The Striker itself is included ("creatures you control").
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ward triggers a counter-unless-pay when an opponent's spell targets it")
    void wardTriggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new ThornfistStriker());
        UUID strikerId = harness.getPermanentId(player1, "Thornfist Striker");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, strikerId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Thornfist Striker");
    }

    @Test
    void payingWardAllowsSpellToResolve() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, striker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(striker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void decliningWardCountersSpellEvenWithManaAvailable() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, striker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(striker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, striker.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(striker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, 0, null, striker.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(striker.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    void realLifeGainEnablesAnthemWithoutBoostingOpponentsAndExpiresNextTurn() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, striker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsLifeGainDoesNotEnableAnthem() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new ThornfistStriker());
        harness.setHand(player1, List.of(new NaturalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
    }
}
