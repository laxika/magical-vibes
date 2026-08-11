package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Colossus of Akros cannot activate monstrosity again")
    void monstrosityOnlyResolvesOnce() {
        addReadyColossus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyColossus() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ColossusOfAkros());
        colossus.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        return colossus;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 10);
    }

    private void declareColossusAttack(Permanent colossus) {
        int colossusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(colossus);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));

        gs.declareAttackers(gd, player1, List.of(colossusIndex));
    }
}
