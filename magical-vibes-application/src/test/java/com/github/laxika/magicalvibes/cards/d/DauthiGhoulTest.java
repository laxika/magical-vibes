package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.r.RollingThunder;
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

@CardUsed({DauthiGhoul.class, FightingDrake.class, RollingThunder.class, SoltariFootSoldier.class})
class DauthiGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature with shadow dies")
    void getsCounterWhenShadowCreatureDies() {
        harness.addToBattlefield(player1, new DauthiGhoul());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        Permanent ghoul = findPermanent(player1, "Dauthi Ghoul");
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
        harness.addToBattlefield(player1, new DauthiGhoul());
        harness.addToBattlefield(player1, new SoltariFootSoldier());

        Permanent ghoul = findPermanent(player1, "Dauthi Ghoul");

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
        harness.addToBattlefield(player1, new DauthiGhoul());
        harness.addToBattlefield(player2, new FightingDrake());

        Permanent ghoul = findPermanent(player1, "Dauthi Ghoul");

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
        harness.addToBattlefield(player1, new DauthiGhoul());
        Permanent firstSoldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());
        Permanent secondSoldier = harness.addToBattlefieldAndReturn(player2, new SoltariFootSoldier());

        Permanent ghoul = findPermanent(player1, "Dauthi Ghoul");

        harness.setHand(player1, List.of(new RollingThunder()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorceryForX(player1, 0, 2,
                Map.of(firstSoldier.getId(), 1, secondSoldier.getId(), 1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
