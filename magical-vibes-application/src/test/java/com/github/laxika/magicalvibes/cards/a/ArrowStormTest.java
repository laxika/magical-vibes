package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheDragonspeaker;
import com.github.laxika.magicalvibes.cards.t.TuskedColossodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArrowStorm.class, GlacialChasm.class, TuskedColossodon.class, SarkhanTheDragonspeaker.class})
class ArrowStormTest extends BaseCardTest {

    @Test
    void dealsFourDamageWithoutRaid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void dealsFiveDamageWithRaid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void normalDamageCanBePrevented() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GlacialChasm());
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void raidDamageCannotBePrevented() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GlacialChasm());
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void dealsFourDamageToCreatureWithoutRaid() {
        var target = harness.addToBattlefieldAndReturn(player2, new TuskedColossodon());
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Tusked Colossodon");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void raidDamageKillsCreatureWithFiveToughness() {
        var target = harness.addToBattlefieldAndReturn(player2, new TuskedColossodon());
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Tusked Colossodon");
        harness.assertInGraveyard(player2, "Tusked Colossodon");
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
    }

    @Test
    void raidDamageRemovesFiveLoyaltyFromPlaneswalker() {
        var target = harness.addToBattlefieldAndReturn(player2, new SarkhanTheDragonspeaker());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Sarkhan, the Dragonspeaker");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }
}
