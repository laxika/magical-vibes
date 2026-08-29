package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroesOfTheRevel.class, GiantGrowth.class, GrizzlyBears.class})
class HeroesOfTheRevelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 1/1 red Satyr token that can't block")
    void entersWithNonblockingSatyrToken() {
        harness.enterBattlefieldAndReturn(player1, new HeroesOfTheRevel());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Satyr")).isEqualTo(1);
        Permanent satyr = findPermanent(player1, "Satyr");
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(1);
        assertThat(bls.canBlock(gd, satyr)).isFalse();
    }

    @Test
    @DisplayName("Boosts your creatures when you cast a spell targeting Heroes of the Revel")
    void boostsYourCreaturesWhenTargetedByOwnSpell() {
        Permanent heroes = harness.addToBattlefieldAndReturn(player1, new HeroesOfTheRevel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(heroes);

        assertThat(gqs.getEffectivePower(gd, heroes)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, heroes)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when your spell targets another creature")
    void doesNotTriggerWhenAnotherCreatureIsTargeted() {
        Permanent heroes = harness.addToBattlefieldAndReturn(player1, new HeroesOfTheRevel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, heroes)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, heroes)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    private void castGiantGrowth(Permanent target) {
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
