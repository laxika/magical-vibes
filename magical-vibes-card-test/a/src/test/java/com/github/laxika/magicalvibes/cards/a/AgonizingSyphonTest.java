package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgonizingSyphon.class, GreenwoodSentinel.class, Forest.class, ChandraNovicePyromancer.class})
class AgonizingSyphonTest extends BaseCardTest {

    @Test
    void dealsDamageToTargetPlayerAndGainsLife() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 18);
    }

    @Test
    void dealsDamageToTargetCreatureAndGainsLife() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, sentinel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    void fizzlesWithoutLifeGainWhenTargetCreatureLeavesBeforeResolution() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, sentinel.getId());
        gd.playerBattlefields.get(player2.getId()).remove(sentinel);
        gd.playerGraveyards.get(player2.getId()).add(sentinel.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }
    @Test
    void dealsDamageToPlaneswalkerAndGainsLife() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetController() {
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsFullLifeEvenWhenAllDamageIsPrevented() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        gd.preventAllDamageToAllCreatures = true;
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new AgonizingSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, sentinel.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(sentinel.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }
}
