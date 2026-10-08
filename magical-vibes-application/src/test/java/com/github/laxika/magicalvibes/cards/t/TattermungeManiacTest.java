package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TattermungeManiac.class, GrizzlyBears.class})
class TattermungeManiacTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring Tattermunge Maniac as attacker succeeds and deals 2 damage")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TattermungeManiac());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declaring no attackers when Tattermunge Maniac can attack throws exception")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new TattermungeManiac());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Tattermunge Maniac while declaring another creature throws exception")
    void mustBeIncludedAmongAttackers() {
        addCreatureReady(player1, new TattermungeManiac());

        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Tattermunge Maniac with summoning sickness is not forced to attack")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new TattermungeManiac());

        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A tapped Tattermunge Maniac is not forced to attack")
    void doesNotAttackWhileTapped() {
        Permanent maniac = addCreatureReady(player1, new TattermungeManiac());
        maniac.tap();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));

        harness.assertLife(player2, 18);
        assertThat(maniac.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Having attacked earlier in the turn does not satisfy a later combat's requirement")
    void mustAttackEvenIfAlreadyAttackedThisTurn() {
        Permanent maniac = addCreatureReady(player1, new TattermungeManiac());
        maniac.setAttackedThisTurn(true);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
