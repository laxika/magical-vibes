package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
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

@CardUsed({SuperAdaptoid.class, SkyknightLegionnaire.class, SerraAngel.class,
        IsamaruHoundOfKonda.class, ShannaSisaysLegacy.class, GrizzlyBears.class})
class SuperAdaptoidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB copies haste and flying as keyword counters")
    void enteringCopiesTargetKeywordsAsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SkyknightLegionnaire());
        harness.setHand(player1, List.of(new SuperAdaptoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent adaptoid = findPermanent(player1, "Super-Adaptoid");
        assertThat(adaptoid.getCounterCount(CounterType.HASTE)).isEqualTo(1);
        assertThat(adaptoid.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, adaptoid, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, adaptoid, Keyword.FLYING)).isTrue();
        assertThat(adaptoid.getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    @DisplayName("Attack copies new keywords without duplicating existing counters")
    void attackingCopiesNewKeywordsWithoutDuplicatingCounters() {
        Permanent targetWithHasteAndFlying = harness.addToBattlefieldAndReturn(
                player1, new SkyknightLegionnaire());
        Permanent targetWithFlyingAndVigilance = harness.addToBattlefieldAndReturn(
                player1, new SerraAngel());
        harness.setHand(player1, List.of(new SuperAdaptoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(targetWithHasteAndFlying.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent adaptoid = findPermanent(player1, "Super-Adaptoid");
        adaptoid.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(adaptoid)));
        harness.handlePermanentChosen(player1, targetWithFlyingAndVigilance.getId());
        harness.passBothPriorities();

        assertThat(adaptoid.getCounterCount(CounterType.HASTE)).isEqualTo(1);
        assertThat(adaptoid.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(adaptoid.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, adaptoid, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Power equals the number of legendary creatures controlled")
    void powerCountsLegendaryCreatures() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuperAdaptoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent adaptoid = findPermanent(player1, "Super-Adaptoid");
        assertThat(gqs.getEffectivePower(gd, adaptoid)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adaptoid)).isEqualTo(2);
    }
}
