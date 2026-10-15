package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BerserkersOfBloodRidge.class, RuneclawBear.class, Pacifism.class})
class BerserkersOfBloodRidgeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Berserkers of Blood Ridge puts it on the battlefield")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BerserkersOfBloodRidge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Berserkers of Blood Ridge");
    }

    @Test
    @DisplayName("Declaring Berserkers of Blood Ridge as attacker succeeds")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BerserkersOfBloodRidge());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Declaring no attackers when Berserkers of Blood Ridge can attack throws exception")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new BerserkersOfBloodRidge());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Berserkers from attackers while declaring other creatures throws exception")
    void mustBeIncludedAmongAttackers() {
        addCreatureReady(player1, new BerserkersOfBloodRidge());

        addCreatureReady(player1, new RuneclawBear());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Berserkers of Blood Ridge does not need to attack if tapped")
    void doesNotAttackIfTapped() {
        Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());
        berserkers.tap();
        addCreatureReady(player1, new RuneclawBear());

        declareAttackers(List.of(1));

        harness.assertLife(player2, 18);
        assertThat(berserkers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Berserkers of Blood Ridge does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        addCreatureReady(player1, new RuneclawBear());

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Berserkers of Blood Ridge deals 4 combat damage when unblocked")
    void dealsFourDamageUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BerserkersOfBloodRidge());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Pacifism prevents attacking despite the attack requirement")
    void doesNotHaveToAttackWhenPacified() {
        Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());
        addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, berserkers.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));

        harness.assertLife(player2, 18);
        assertThat(berserkers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untapped Berserkers must attack again in a second combat")
    void mustAttackInEachCombat() {
        Permanent berserkers = addCreatureReady(player1, new BerserkersOfBloodRidge());
        declareAttackers(List.of(0));
        harness.assertLife(player2, 16);
        berserkers.untap();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0));
        harness.assertLife(player2, 12);
    }
}
