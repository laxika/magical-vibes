package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScarletSpiderBenReilly.class, GrizzlyBears.class})
class ScarletSpiderBenReillyTest extends BaseCardTest {

    @Test
    @DisplayName("Web-slinging gives Scarlet Spider counters equal to the returned creature's mana value")
    void webSlingingUsesReturnedCreatureManaValue() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();

        Permanent scarletSpider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ScarletSpiderBenReilly)
                .findFirst()
                .orElseThrow();
        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(tappedCreature.getCard());
    }

    @Test
    @DisplayName("A normal cast does not get Sensational Save counters")
    void normalCastDoesNotGetCounters() {
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scarletSpider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ScarletSpiderBenReilly)
                .findFirst()
                .orElseThrow();
        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
