package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VultureFeatheredFiend.class, AirElemental.class, Forest.class,
        GrizzlyBears.class, SuntailHawk.class})
class VultureFeatheredFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on each flying combat damage dealer and draws once")
    void rewardsFlyingCombatDamageDealers() {
        harness.addToBattlefield(player1, new VultureFeatheredFiend());
        Permanent hawk = addReadyAttacker(new SuntailHawk());
        Permanent elemental = addReadyAttacker(new AirElemental());
        Permanent bears = addReadyAttacker(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(hawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for a nonflying creature")
    void ignoresNonflyingCombatDamageDealers() {
        harness.addToBattlefield(player1, new VultureFeatheredFiend());
        Permanent bears = addReadyAttacker(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private Permanent addReadyAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }
}
