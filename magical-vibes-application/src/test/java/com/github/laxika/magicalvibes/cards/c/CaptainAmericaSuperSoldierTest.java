package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgentMariaHill;
import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaSuperSoldier.class, AgentMariaHill.class, GoblinHero.class, Shock.class, Murder.class})
class CaptainAmericaSuperSoldierTest extends BaseCardTest {

    @Test
    @CardUsed(Banefire.class)
    void unpreventableDamageRemovesOnlyOneShieldCounter() {
        Permanent captain = harness.enterBattlefieldAndReturn(player2, new CaptainAmericaSuperSoldier());
        captain.setCounterCount(CounterType.SHIELD, 2);
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new Banefire()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 5, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(captain.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(captain);
    }

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent captain = harness.enterBattlefieldAndReturn(player1, new CaptainAmericaSuperSoldier());

        assertThat(captain.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shield counter prevents damage and turns off the static abilities when removed")
    void shieldCounterPreventsDamageAndTurnsOffAbilities() {
        Permanent captain = harness.enterBattlefieldAndReturn(player1, new CaptainAmericaSuperSoldier());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());
        Permanent nonHero = addCreatureReady(player1, new GoblinHero());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, captain, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHero, Keyword.HEXPROOF)).isFalse();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(captain.getMarkedDamage()).isZero();
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
    }

    @Test
    @DisplayName("Shield counter replaces destruction")
    void shieldCounterReplacesDestruction() {
        Permanent captain = harness.enterBattlefieldAndReturn(player1, new CaptainAmericaSuperSoldier());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
    }
}
