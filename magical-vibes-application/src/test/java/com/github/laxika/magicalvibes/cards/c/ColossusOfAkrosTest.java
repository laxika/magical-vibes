package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SipOfHemlock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossusOfAkros.class, SipOfHemlock.class})
class ColossusOfAkrosTest extends BaseCardTest {

    @Test
    @DisplayName("Colossus of Akros has defender and cannot attack before becoming monstrous")
    void cannotAttackBeforeBecomingMonstrous() {
        Permanent colossus = addReadyColossus();

        assertThat(gqs.hasKeyword(gd, colossus, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.DEFENDER)).isTrue();
        assertThatThrownBy(() -> declareColossusAttack(colossus))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Monstrosity puts ten counters on Colossus of Akros and lets it attack with trample")
    void becomingMonstrousAddsCountersAndAttackPermissions() {
        Permanent colossus = addReadyColossus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.DEFENDER)).isTrue();

        declareColossusAttack(colossus);
        assertThat(colossus.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing when already monstrous")
    void monstrosityCanBeActivatedAgainWithoutAddingCounters() {
        Permanent colossus = addReadyColossus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(colossus.isMonstrous()).isTrue();
    }

    @Test
    void multiplePendingMonstrosityActivationsOnlyAddCountersOnce() {
        Permanent colossus = addReadyColossus();
        harness.addMana(player1, ManaColor.COLORLESS, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(colossus.isMonstrous()).isTrue();
    }

    @Test
    void removingCountersDoesNotRemoveMonstrousBenefits() {
        Permanent colossus = addReadyColossus();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        colossus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(colossus.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.TRAMPLE)).isTrue();
        declareColossusAttack(colossus);
        assertThat(colossus.isAttacking()).isTrue();
    }

    @Test
    void countersAloneDoNotGrantMonstrousBenefits() {
        Permanent colossus = addReadyColossus();
        colossus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        assertThat(colossus.isMonstrous()).isFalse();
        assertThat(gqs.hasKeyword(gd, colossus, Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> declareColossusAttack(colossus))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void indestructiblePreventsDestruction() {
        Permanent colossus = addReadyColossus();
        harness.setHand(player2, List.of(new SipOfHemlock()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, colossus.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Colossus of Akros");
        harness.assertLife(player1, 18);
    }

    private Permanent addReadyColossus() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ColossusOfAkros());
        colossus.setSummoningSick(false);
        return colossus;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 10);
    }

    private void declareColossusAttack(Permanent colossus) {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        int colossusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(colossus);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(colossusIndex));
    }
}
