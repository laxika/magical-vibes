package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VultureFeatheredFiend.class, AirElemental.class, Forest.class,
        GrizzlyBears.class, SuntailHawk.class, Unsummon.class})
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

    @Test
    @DisplayName("Vulture rewards its own flying combat damage")
    void rewardsItsOwnCombatDamage() {
        Permanent vulture = addReadyAttacker(new VultureFeatheredFiend());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(vulture.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Still draws when the only damage dealer leaves before resolution")
    void drawsAfterDamageDealerLeaves() {
        harness.addToBattlefield(player1, new VultureFeatheredFiend());
        Permanent hawk = addReadyAttacker(new SuntailHawk());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hawk);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The trigger resolves after Vulture leaves the battlefield")
    void resolvesAfterVultureLeaves() {
        Permanent vulture = harness.addToBattlefieldAndReturn(player1, new VultureFeatheredFiend());
        Permanent hawk = addReadyAttacker(new SuntailHawk());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, vulture.getId());
        resolveAllTriggers();

        assertThat(hawk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    private Permanent addReadyAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }
}
