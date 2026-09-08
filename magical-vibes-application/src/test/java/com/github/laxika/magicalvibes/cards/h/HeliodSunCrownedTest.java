package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliodSunCrowned.class, AngelOfMercy.class, GrizzlyBears.class, SuntailHawk.class})
class HeliodSunCrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Heliod is not a creature below five devotion to white")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent heliod = addHeliod();

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Heliod becomes a creature at five devotion to white")
    void becomesCreatureAtDevotionThreshold() {
        Permanent heliod = addHeliod();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }

        assertThat(gqs.isCreature(gd, heliod)).isTrue();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Life gain puts a +1/+1 counter on a target creature or enchantment you control")
    void lifeGainCountersTargetEnchantment() {
        Permanent heliod = addHeliod();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, heliod.getId());
        harness.passBothPriorities();

        assertThat(heliod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gain can target a creature you control")
    void lifeGainCountersTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addHeliod();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability grants lifelink to another creature until end of turn")
    void grantsLifelinkToAnotherCreature() {
        Permanent heliod = addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target Heliod itself")
    void cannotTargetHeliodItself() {
        Permanent heliod = addHeliod();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, heliod.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHeliod() {
        return harness.addToBattlefieldAndReturn(player1, new HeliodSunCrowned());
    }
}
