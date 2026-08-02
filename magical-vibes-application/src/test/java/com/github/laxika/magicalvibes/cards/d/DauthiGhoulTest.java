package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DauthiGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature with shadow dies")
    void getsCounterWhenShadowCreatureDies() {
        harness.addToBattlefield(player1, new DauthiGhoul());
        harness.addToBattlefield(player2, new SoltariFootSoldier());

        Permanent ghoul = findPermanent(player1, "Dauthi Ghoul");
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID soldierId = harness.getPermanentId(player2, "Soltari Foot Soldier");
        harness.castInstant(player1, 0, soldierId);
        harness.passBothPriorities(); // Resolve Shock -> soldier dies -> death trigger
        harness.passBothPriorities(); // Resolve the +1/+1 counter trigger

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

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID soldierId = harness.getPermanentId(player1, "Soltari Foot Soldier");
        harness.castInstant(player1, 0, soldierId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature without shadow dies")
    void noCounterWhenNonShadowCreatureDies() {
        harness.addToBattlefield(player1, new DauthiGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ghoul = findPermanent(player1, "Dauthi Ghoul");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
