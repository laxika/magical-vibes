package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MangledSoulrager.class, GiantSpider.class})
class MangledSoulragerTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters ability switches all creatures and creates the boon")
    void entersAndCreatesBoon() {
        Permanent opposingSpider = harness.enterBattlefieldAndReturn(player2, new GiantSpider());
        Permanent soulrager = harness.enterBattlefieldAndReturn(player1, new MangledSoulrager());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soulrager)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soulrager)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingSpider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingSpider)).isEqualTo(2);

        Permanent entered = harness.enterBattlefieldAndReturn(player1, new GiantSpider());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(2);

        Permanent opposingEntered = harness.enterBattlefieldAndReturn(player2, new GiantSpider());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingEntered)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingEntered)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boon switches only its next twelve controlled creatures")
    void boonExpiresAfterTwelveUses() {
        harness.enterBattlefieldAndReturn(player1, new MangledSoulrager());
        harness.passBothPriorities();

        List<Permanent> switched = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Permanent spider = harness.enterBattlefieldAndReturn(player1, new GiantSpider());
            harness.passBothPriorities();
            switched.add(spider);
        }
        Permanent thirteenth = harness.enterBattlefieldAndReturn(player1, new GiantSpider());
        harness.passBothPriorities();

        assertThat(switched).allSatisfy(spider -> {
            assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
        });
        assertThat(gqs.getEffectivePower(gd, thirteenth)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thirteenth)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MangledSoulrager()));
        harness.setLibrary(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mangled Soulrager");
        harness.assertInHand(player1, "Giant Spider");
    }
}
