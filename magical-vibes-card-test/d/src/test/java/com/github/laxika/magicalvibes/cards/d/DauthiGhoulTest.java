package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.r.RollingThunder;
import com.github.laxika.magicalvibes.cards.s.ShadowRift;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauthiGhoul.class, FightingDrake.class, RollingThunder.class, ShadowRift.class, SoltariFootSoldier.class})
class DauthiGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature with shadow dies")
    void getsCounterWhenShadowCreatureDies() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DauthiGhoul());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID soldierId = harness.getPermanentId(player2, "Soltari Foot Soldier");
        harness.castSorceryForX(player1, 0, 1, Map.of(soldierId, 1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ghoul)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ghoul)).isEqualTo(2);
    }

    @Test
    @DisplayName("Also triggers for a creature with shadow its controller owns")
    void getsCounterWhenAllyShadowCreatureDies() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DauthiGhoul());
        harness.addToBattlefield(player1, new SoltariFootSoldier());

        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID soldierId = harness.getPermanentId(player1, "Soltari Foot Soldier");
        harness.castSorceryForX(player1, 0, 1, Map.of(soldierId, 1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature without shadow dies")
    void noCounterWhenNonShadowCreatureDies() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DauthiGhoul());
        harness.addToBattlefield(player2, new FightingDrake());

        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID drakeId = harness.getPermanentId(player2, "Fighting Drake");
        harness.castSorceryForX(player1, 0, 4, Map.of(drakeId, 4));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers once for each shadow creature that dies simultaneously")
    void getsOneCounterPerShadowCreatureThatDies() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DauthiGhoul());
        Permanent firstSoldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        Permanent secondSoldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());

        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorceryForX(player1, 0, 2,
                Map.of(firstSoldier.getId(), 1, secondSoldier.getId(), 1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers when a creature with temporarily granted shadow dies")
    void getsCounterWhenCreatureWithGrantedShadowDies() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new DauthiGhoul());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new FightingDrake());
        harness.setLibrary(player1, List.of(new SoltariFootSoldier()));
        harness.setHand(player1, List.of(new ShadowRift(), new RollingThunder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, drake.getId());
        harness.castSorceryForX(player1, 0, 4, Map.of(drake.getId(), 4));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(drake);
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
