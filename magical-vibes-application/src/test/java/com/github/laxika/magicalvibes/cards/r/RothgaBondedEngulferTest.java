package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RothgaBondedEngulfer.class, GiantSpider.class, GrizzlyBears.class})
class RothgaBondedEngulferTest extends BaseCardTest {

    @Test
    void nextCreatureSpellPerpetuallyGetsPlusPowerPlusPower() {
        RothgaBondedEngulfer rothga = new RothgaBondedEngulfer();
        GiantSpider spider = new GiantSpider();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(rothga, spider, bears));
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent enteredSpider = findPermanentByCard(player1, spider);
        Permanent enteredBears = findPermanentByCard(player1, bears);
        assertThat(gqs.getEffectivePower(gd, enteredSpider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enteredSpider)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, enteredBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteredBears)).isEqualTo(2);
    }

    private Permanent findPermanentByCard(com.github.laxika.magicalvibes.model.Player player,
                                          com.github.laxika.magicalvibes.model.Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
