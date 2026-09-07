package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfRavnica;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class,
        InvasionOfRavnica.class, TandemTakedown.class})
class TandemTakedownTest extends BaseCardTest {

    @Test
    void boostsOneCreatureBeforeItDealsPowerDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castInstant(player1, 0, List.of(victim.getId(), source.getId()));
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(victim.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void bothSelectedCreaturesAreBoostedAndDealDamage() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CrawWurm());
        Permanent victim = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castInstant(player1, 0, List.of(victim.getId(), firstSource.getId(), secondSource.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(firstSource.getEffectivePower()).isEqualTo(4);
        assertThat(secondSource.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void canDealDamageToABattle() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRavnica());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        harness.castInstant(player1, 0, List.of(battle.getId(), source.getId()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    void recipientMustBeDifferentFromTheSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new TandemTakedown()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
