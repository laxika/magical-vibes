package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisciplinedDuelist.class, Shock.class, Strangle.class, Murder.class, DealGoneBad.class})
class DisciplinedDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent duelist = castDuelist();

        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its shield counter prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent duelist = castDuelist();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, duelist.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(duelist);
        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(duelist.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A second damage event kills it after its shield is consumed")
    void secondDamageEventKillsDuelist() {
        Permanent duelist = castDuelist();
        harness.setHand(player1, List.of(new Strangle(), new Strangle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, duelist.getId());
        harness.assertOnBattlefield(player1, "Disciplined Duelist");
        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isZero();

        harness.castAndResolveSorcery(player1, 0, duelist.getId());
        harness.assertNotOnBattlefield(player1, "Disciplined Duelist");
        harness.assertInGraveyard(player1, "Disciplined Duelist");
    }

    @Test
    @DisplayName("A shield replaces destruction once without tapping the creature")
    void shieldReplacesDestructionOnce() {
        Permanent duelist = castDuelist();
        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, duelist.getId());
        harness.assertOnBattlefield(player1, "Disciplined Duelist");
        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(duelist.isTapped()).isFalse();

        harness.castAndResolveInstant(player1, 0, duelist.getId());
        harness.assertNotOnBattlefield(player1, "Disciplined Duelist");
        harness.assertInGraveyard(player1, "Disciplined Duelist");
    }

    @Test
    @DisplayName("A shield does not protect against zero toughness")
    void shieldDoesNotPreventZeroToughnessDeath() {
        Permanent duelist = castDuelist();
        harness.setHand(player1, List.of(new DealGoneBad()));
        harness.setLibrary(player2, List.of(new DisciplinedDuelist(),
                new DisciplinedDuelist(), new DisciplinedDuelist()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, List.of(duelist.getId(), player2.getId()));

        harness.assertNotOnBattlefield(player1, "Disciplined Duelist");
        harness.assertInGraveyard(player1, "Disciplined Duelist");
        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unblocked Duelist deals damage in both combat damage steps")
    void doubleStrikeDealsFourDamage() {
        Permanent duelist = castDuelist();
        duelist.setSummoningSick(false);
        duelist.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    private Permanent castDuelist() {
        harness.castFromHand(player1, new DisciplinedDuelist(), "{G}{W}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Disciplined Duelist");
    }
}
