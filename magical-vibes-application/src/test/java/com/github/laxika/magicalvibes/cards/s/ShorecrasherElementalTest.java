package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ShorecrasherElemental.class)
class ShorecrasherElementalTest extends BaseCardTest {

    @Test
    void megamorphPutsACounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new ShorecrasherElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Shorecrasher Elemental");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental));
        harness.passBothPriorities();

        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blueAbilityReturnsItFaceDownUnderItsOwnersControl() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Shorecrasher Elemental");
        assertThat(returned).isNotSameAs(elemental);
        assertThat(returned.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void firstModeGivesItPlusOneMinusOne() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(1);
        assertThat(elemental.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void secondModeGivesItMinusOnePlusOne() {
        Permanent elemental = addCreatureReady(player1, new ShorecrasherElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, 1, null);
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(-1);
        assertThat(elemental.getToughnessModifier()).isEqualTo(1);
    }
}
